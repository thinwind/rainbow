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
package win.shangyh.datatrans.rainbow.web.impl;

import java.sql.Connection;

import com.lmax.disruptor.dsl.Disruptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import win.shangyh.datatrans.rainbow.config.ReadStrQueueConfig;
import win.shangyh.datatrans.rainbow.config.WriteDbQuqueConfig;
import win.shangyh.datatrans.rainbow.connection.RainbowPool;
import win.shangyh.datatrans.rainbow.data.BatchProcessor;
import win.shangyh.datatrans.rainbow.data.BatchProcessorRegister;
import win.shangyh.datatrans.rainbow.data.RowRecord;
import win.shangyh.datatrans.rainbow.processor.RowProcessorFactory;
import win.shangyh.datatrans.rainbow.queue.QueueFactory;
import win.shangyh.datatrans.rainbow.queue.QueueRegister;
import win.shangyh.datatrans.rainbow.web.QueueService;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-16  02:08
 *
 */
@Service
public class QueueServiceImpl implements QueueService {

    @Value("${rb.column.separator}")
    private String columnSeparator;

    @Autowired
    RowProcessorFactory rowProcessorFactory;

    @Autowired
    RainbowPool poolManager;

    @Autowired
    QueueFactory queueFactory;

    @Autowired
    private WriteDbQuqueConfig writeDbQuqueConfig;

    @Autowired
    private ReadStrQueueConfig readStrQueueConfig;

    @Autowired
    Disruptor<RowRecord> dbQueue;

    @Override
    public void registerRowDataProcessor(String tableName)
            throws Exception {
        Connection connection = poolManager.getPooledConnection();
        rowProcessorFactory.registerRowDataProcessor(tableName, connection, columnSeparator);
        connection.commit();
        connection.close();

        var rowProcessor = rowProcessorFactory.getRowDataProcessor(tableName);
        rowProcessor.getInsertSql();
        var builder = BatchProcessor.builder();
        builder.queueSize(writeDbQuqueConfig.getSize())
                .batchSize(writeDbQuqueConfig.getBatchSize())
                .disruptor(dbQueue)
                .insertSql(rowProcessor.getInsertSql())
                .columnTypes(rowProcessor.getColumnTypes())
                .tableName(tableName);
        var batchProcessor = builder.build();
        BatchProcessorRegister.registerBatchProcessor(tableName, batchProcessor);
    }

    public synchronized void registerReadQueue(String tableName) {
        var queue = QueueRegister.getQueue(tableName);
        if (queue != null) {
            return;
        }
        queue = queueFactory.readFileQueue(readStrQueueConfig, tableName);
        QueueRegister.registerQueue(tableName, queue);
    }

    @Override
    public synchronized void unregisterReadQueue(String tableName) throws Exception {
        var queue = QueueRegister.getQueue(tableName);
        if (queue != null) {
            queue.shutdown();
        }
        QueueRegister.unregisterQueue(tableName);
    }

}
