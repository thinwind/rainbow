/* 
 * Copyright 2024 Shang Yehua <niceshang@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package win.shangyh.datatrans.rainbow.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import win.shangyh.datatrans.rainbow.config.ReadStrQueueConfig;
import win.shangyh.datatrans.rainbow.config.WriteDbQuqueConfig;
import win.shangyh.datatrans.rainbow.connection.RainbowPool;
import win.shangyh.datatrans.rainbow.data.BatchProcessor;
import win.shangyh.datatrans.rainbow.data.BatchProcessorRegister;
import win.shangyh.datatrans.rainbow.data.LineCounter;
import win.shangyh.datatrans.rainbow.data.RowRecord;
import win.shangyh.datatrans.rainbow.data.RowString;
import win.shangyh.datatrans.rainbow.data.TableCounter;
import win.shangyh.datatrans.rainbow.processor.RowDataProcessor;
import win.shangyh.datatrans.rainbow.processor.RowProcessorFactory;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-15  19:02
 *
 */
public class QueueFactory {

    // private final Map<String, Disruptor<RowString>> readQueueMap=new ConcurrentHashMap<>();
    private final RowProcessorFactory rowProcessorFactory;

    public QueueFactory(RowProcessorFactory rowProcessorFactory) {
        this.rowProcessorFactory = rowProcessorFactory;
    }

    public Disruptor<RowString> readFileQueue(ReadStrQueueConfig readStrQueueConfig, String tableName) {
        //disruptor队列
        Disruptor<RowString> disruptor = new Disruptor<>(RowString::new, readStrQueueConfig.getSize(),
                DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new SleepingWaitStrategy());
        //线程池大小
        int taskCount = readStrQueueConfig.getConsumer();

        @SuppressWarnings("unchecked")
        WorkHandler<RowString>[] handlerPool = new WorkHandler[taskCount];
        for (int i = 0; i < taskCount; i++) {
            handlerPool[i] = createWorkHandler(tableName);
        }
        // disruptor.handleEventsWith(handlerPool);
        disruptor.handleEventsWithWorkerPool(handlerPool);

        Logger queueLogger = LoggerFactory.getLogger("FileReadQueue");

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

    private WorkHandler<RowString> createWorkHandler(String tableName) {
        return event -> {
            try {
                BatchProcessor batchProcessor = BatchProcessorRegister.getBatchProcessor(tableName);
                RowDataProcessor processor = rowProcessorFactory.getRowDataProcessor(tableName);
                var row = processor.parseRow(event.getRow(), event.getColums());
                batchProcessor.addRow(event.id, row);
            } finally {
                LineCounter.incrementAndGet(tableName);
                event.clear();
            }
        };
    }

    public Disruptor<RowRecord> writeDbQueue(WriteDbQuqueConfig config, RainbowPool pool) {
        Disruptor<RowRecord> disruptor = new Disruptor<>(RowRecord::new, config.getSize(),
                DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new SleepingWaitStrategy());
        Logger queueLogger = LoggerFactory.getLogger("DatabaseWriterQueue");
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<RowRecord>() {

            @Override
            public void handleEventException(Throwable ex, long sequence, RowRecord event) {
                //首先记录异常信息
                queueLogger.error("Write row data error", ex);
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

        @SuppressWarnings("unchecked")
        WorkHandler<RowRecord>[] handlerPool = new WorkHandler[config.getConsumer()];
        for (int i = 0; i < handlerPool.length; i++) {
            handlerPool[i] = createDbHandler(pool);
        }
        // disruptor.handleEventsWith(handlerPool);
        disruptor.handleEventsWithWorkerPool(handlerPool);

        disruptor.start();
        return disruptor;
    }

    private WorkHandler<RowRecord> createDbHandler(RainbowPool pool) {
        return event -> {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            int cnt = 0;
            try {
                connection = pool.getPooledConnection();
                Object[][] rows = event.getValues();
                if (rows == null || rows.length == 0) {
                    event.clear();
                    return;
                }

                if (rows.length == 1) {
                    cnt = 1;
                    // 单条执行
                    Object[] values = rows[0];
                    preparedStatement = connection.prepareStatement(event.getInsertSql());
                    int[] columnTypes = event.getColumnTypes();
                    for (int i = 0; i < values.length; i++) {
                        preparedStatement.setObject(i + 1, values[i], columnTypes[i]);
                    }
                } else {
                    // 批量执行
                    int[] columnTypes = event.getColumnTypes();
                    preparedStatement = connection.prepareStatement(event.getInsertSql());
                    for (int i = 0; i < rows.length; i++) {
                        var values = rows[i];
                        if (values == null || values.length == 0) {
                            break;
                        }
                        cnt++;
                        for (int j = 0; j < values.length; j++) {
                            preparedStatement.setObject(j + 1, values[j], columnTypes[j]);
                        }
                        preparedStatement.addBatch();
                    }
                    preparedStatement.executeBatch();
                }

            } finally {
                TableCounter.incrementAndGet(event.getTableName(), cnt);
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.commit();
                    connection.close();
                }
                event.clear();
            }
        };
    }
}
