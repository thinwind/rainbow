/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package win.shangyh.datatrans.rainbow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.yunxiao.afu.common.ResultEntity;
import com.alibaba.yunxiao.afu.domain.TaskType;
import com.alibaba.yunxiao.afu.util.AegisConfigClientWrapper;
import com.lmax.disruptor.LiteTimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

@Configuration
public class DisruptorConfig {

    @Autowired
    ExecutorService executorService;

    //Disruptor超时时间：15min
    private final static int DEFAULT_TASK_TIME_OUT = 15;

    //数据交换超时时间10sec
    //如果10秒都没有响应，说明controller发生异常，不用再等
    private final static int DEFAULT_EXCHANGER_TIME_OUT = 10;

    //Disruptor的buffer size，只能为2的指数次幂
    //默认1024个位置，对于大多数场景已经足够
    private final static int DISRUPTOR_BUFFER_SIZE = 1024;

//    public final static Map<String, Object> DISRUPTOR_CONFIG = new LinkedHashMap<>();

    //成功执行的任务的数量
    //此数量表示自系统启动以来执行的数量
    //每次启动都会清零
    public final static AtomicInteger SUCCESS_TASK_COUNT = new AtomicInteger(0);
    //执行失败的任务的数量
    public final static AtomicInteger FAILED_TASK_COUNT = new AtomicInteger(0);

    private int maxTaskCount;

    /**
     * 必须在执行线程池启动以后，才可以初始化
     * @return
     */
    @Bean("task-disruptor")
    public Disruptor<TaskEvent> disruptorInstance() {
        //初始化执行队列的日志
        final Log log = LogFactory.getLog("disruptor-task");
        //disruptor队列
        Disruptor<TaskEvent> disruptor = new Disruptor<>(TaskEvent::new, DISRUPTOR_BUFFER_SIZE,
                DaemonThreadFactory.INSTANCE, ProducerType.MULTI,
                new LiteTimeoutBlockingWaitStrategy(DEFAULT_TASK_TIME_OUT, TimeUnit.MINUTES));

        //disruptor任务处理器，数量与线程池大小一样，这样保证分发任务时，同时执行任务数不会超过
        //线程池大小
        int taskCount = getTaskCount(log);
        DISRUPTOR_INFO.setMaxTaskCount(taskCount);
        WorkHandler<TaskEvent>[] handlerPool = new WorkHandler[taskCount];
        for (int i = 0; i < taskCount; i++) {
            handlerPool[i] = createWorkHandler();
        }
        disruptor.handleEventsWithWorkerPool(handlerPool);
        disruptor.setDefaultExceptionHandler(getExceptionHandler(log));
        disruptor.start();
        return disruptor;
    }

    private ExceptionHandler<TaskEvent> getExceptionHandler(Log log) {
        return new ExceptionHandler<TaskEvent>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, TaskEvent event) {
                FAILED_TASK_COUNT.getAndIncrement();
                //首先记录异常信息
                log.error("任务执行失败", ex);
                //返回信息
                ResultEntity result = ResultEntity.newFailInstance();
                String msg = ex.getMessage();
                //NPE时，message是null
                if (msg == null) {
                    msg = ex.toString();
                }
                result.setErrorMsg(msg);
                //输出结果
                try {
                    event.getExchanger().exchange(result, DEFAULT_EXCHANGER_TIME_OUT, TimeUnit.SECONDS);
                } catch (Exception e) {
                    //写出错误的时候，不要再抛出异常
                    //无法写出，通常代表连接已经断开，不需要再处理
                    //直接结束即可
                    log.error("输出结果错误", e);
                } finally {
                    //最后清理资源
                    event.clear();
                }
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                log.error("disruptor启动失败", ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                log.error("disruptor关闭失败", ex);
            }
        };
    }

    private WorkHandler<TaskEvent> createWorkHandler() {
        return (event) -> {
            TaskType taskType = event.getTaskType();
            //执行任务
            //SQL执行任务有两个入口
            //AfuExecuteController#executeTask和
            //AfuExecuteController#executeSql
            //databank的执行入口是executeTask，
            //对外调用是executeSql，两种接口的数据格式是不同的
            //一方面是因为executeSql公布较早，对外的数据格式已经固化
            //executeTask是databank的统一任务入口，数据格式也是统一的
            //两个入口的数据不同，因此需要分别处理
            //在executeSql入口的数据，是没有taskType信息的，因此，使用
            //taskType是否为null来区分数据来源和执行的方式
            ResultEntity result;
            if (taskType == null) {
                result = executorService.executeSql(event.getSqlDto());
            } else {
                result = taskExecutorService.executeTask(event.getTaskType(), event.getRunArg(), event.getParams());
            }
            //输出结果
            event.getExchanger().exchange(result, DEFAULT_EXCHANGER_TIME_OUT, TimeUnit.SECONDS);
            //记录执行和失败的数量
            if(result.isSuccess()){
                SUCCESS_TASK_COUNT.incrementAndGet();
            }else{
                FAILED_TASK_COUNT.incrementAndGet();
            }
            //最后清理资源
            event.clear();
        };
    }

    private int getTaskCount(Log log) {
        int taskCount;
        try {
            final String configCount = AegisConfigClientWrapper.getKey("afu.task.count");
            taskCount = Integer.parseInt(configCount);
            //至少两个任务数
            //否则会造成任务挤压，也不利于cpu资源充分利用
            if (taskCount < 2) {
                taskCount = 2;
            }
        } catch (Exception e) {
            //读取或者解析失败，使用默认配置
            log.warn("从配置中心读取最大任务数失败，使用默认值:" + maxTaskCount);
            taskCount = maxTaskCount;
        }
        return taskCount;
    }

    /**
     * 关闭前释放资源
     */
    @PreDestroy
    public void close() {
        final Log log = LogFactory.getLog("PreDestroy");
        Disruptor<TaskEvent> disruptor = disruptorInstance();
        try {
            disruptor.shutdown();
        } catch (Exception e) {
            log.error("关闭线程池失败", e);
        }
    }
}
