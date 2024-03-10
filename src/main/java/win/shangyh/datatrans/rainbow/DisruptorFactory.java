/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package win.shangyh.datatrans.rainbow;

import java.sql.Connection;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import win.shangyh.datatrans.rainbow.config.ReadStrQueueConfig;
import win.shangyh.datatrans.rainbow.config.WriteDbQuqueConfig;
import win.shangyh.datatrans.rainbow.processor.RowDataProcessor;
import win.shangyh.datatrans.rainbow.processor.RowProcessorFactory;

public class DisruptorFactory {

    private final static Logger queueLogger = LoggerFactory.getLogger("WorkerQueue");

    private final RowProcessorFactory rowProcessorFactory;

    public DisruptorFactory(RowProcessorFactory rowProcessorFactory) {
        this.rowProcessorFactory = rowProcessorFactory;
    }

    /**
     * 必须在执行线程池启动以后，才可以初始化
     * @return
     */
    public Disruptor<RowString> readDisruptor(ReadStrQueueConfig readStrQueueConfig, String tableName,
            Disruptor<RowRecord> writeQueue, ConnectionPoolManager manager) {
        //disruptor队列
        Disruptor<RowString> disruptor = new Disruptor<>(RowString::new, readStrQueueConfig.getSize(),
                DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new SleepingWaitStrategy());
        //线程池大小
        int taskCount = readStrQueueConfig.getConsumer();

        @SuppressWarnings("unchecked")
        EventHandler<RowString>[] handlerPool = new EventHandler[taskCount];
        for (int i = 0; i < taskCount; i++) {
            handlerPool[i] = createWorkHandler(writeQueue, manager,
                    rowProcessorFactory.getRowDataProcessor(tableName));
        }
        disruptor.handleEventsWith(handlerPool);
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<RowString>() {

            @Override
            public void handleEventException(Throwable ex, long sequence, RowString event) {
                //首先记录异常信息
                queueLogger.error("Process row data error,row string:{}", event.getRow(), ex);
                event.clear();
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                queueLogger.error("Read Disruptor starts error", ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                queueLogger.error("Read Disruptor shutdown error", ex);
            }

        });
        disruptor.start();
        return disruptor;
    }

    public Disruptor<RowRecord> writeDisruptor(WriteDbQuqueConfig config) {
        //disruptor队列
        Disruptor<RowRecord> disruptor = new Disruptor<>(RowRecord::new, config.getSize(),
                DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new SleepingWaitStrategy());

        //disruptor任务处理器，数量与线程池大小一样，这样保证分发任务时，同时执行任务数不会超过
        //线程池大小
        int taskCount = config.getConsumer();

        @SuppressWarnings("unchecked")
        EventHandler<RowRecord>[] handlerPool = new EventHandler[taskCount];
        for (int i = 0; i < taskCount; i++) {
            handlerPool[i] = createWriteHandler();
        }
        disruptor.handleEventsWith(handlerPool);
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<RowRecord>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, RowRecord event) {
                //首先记录异常信息
                queueLogger.error("Write into database error", ex);
                try {
                    if(event.connection != null){
                        event.connection.close();
                    }
                } catch (Exception e) {
                    queueLogger.error("Closing connection error", e);
                }

                //最后清理资源
                event.clear();
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                queueLogger.error("Write Disruptor starts error", ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                queueLogger.error("Write Disruptor shutdown error", ex);
            }
        });
        disruptor.start();
        return disruptor;
    }

    private EventHandler<RowRecord> createWriteHandler() {
        return (event, sequence, endOfBatch) -> {
            //执行写入
            try {
                event.preparedStatement.execute();
                event.connection.commit();
                event.connection.close();
            } catch (Exception e) {
                queueLogger.error("Write data error", e);
                //首先记录异常信息
                if(event.connection != null){
                    event.connection.rollback();
                }
            }

            //最后清理资源
            event.clear();
        };
    }

    private EventHandler<RowString> createWorkHandler(Disruptor<RowRecord> writeDisruptor,
            ConnectionPoolManager manager,
            RowDataProcessor rowDataProcessor) {
        return (event, sequence, endOfBatch) -> {
            //获取行数据
            String row = event.getRow();
            String[] columns = event.getColums();
            //解析数据
            Object[] rowValues = rowDataProcessor.parseRow(row,columns);
            String[] rowTitles = rowDataProcessor.getRowTitles();
            String tableName = rowDataProcessor.getTableName();

            StringBuilder builder = new StringBuilder();
            builder.append("insert into ").append(tableName).append(" (");
            Object[] params = new Object[rowValues.length];
            int idx = 0;

            for (int i = 0; i < rowValues.length; i++) {
                if (rowValues[i] != null) {
                    builder.append(rowTitles[i]).append(",");
                    params[idx++] = rowValues[i];
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(") values (");
            for (int i = 0; i < idx; i++) {
                builder.append("?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");

            Connection connection = manager.getPooledConnection();
            var preparedStatement = connection.prepareStatement(builder.toString());
            for (int i = 0; i < idx; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            //发布写入任务
            writeDisruptor.publishEvent((rec, recSeq) -> {
                rec.connection = connection;
                rec.preparedStatement = preparedStatement;
            });
            //最后清理资源
            event.clear();
        };
    }
}
