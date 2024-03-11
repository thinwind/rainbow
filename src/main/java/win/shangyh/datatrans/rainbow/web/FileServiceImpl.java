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
package win.shangyh.datatrans.rainbow.web;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lmax.disruptor.dsl.Disruptor;

import win.shangyh.datatrans.rainbow.ConnectionPoolManager;
import win.shangyh.datatrans.rainbow.DisruptorFactory;
import win.shangyh.datatrans.rainbow.RowRecord;
import win.shangyh.datatrans.rainbow.RowString;
import win.shangyh.datatrans.rainbow.config.ReadStrQueueConfig;
import win.shangyh.datatrans.rainbow.config.WriteDbQuqueConfig;
import win.shangyh.datatrans.rainbow.processor.RowDataProcessor;
import win.shangyh.datatrans.rainbow.processor.RowProcessorFactory;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-09  08:49
 *
 */
@Service
public class FileServiceImpl implements FileService{
    
    @Autowired
    RowProcessorFactory rowProcessorFactory;
    
    @Autowired
    ConnectionPoolManager poolManager;
    
    @Autowired
    DisruptorFactory disruptorFactory;

    @Autowired
    private WriteDbQuqueConfig writeDbQuqueConfig;

    @Autowired
    private ReadStrQueueConfig readStrQueueConfig;

    private final Map<String,Disruptor<RowString>> tableQueue = new ConcurrentHashMap<>();

    private Disruptor<RowRecord> dbWriterQueue;

    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    @Value("${rb.column.separator}")
    private String columnSeparator;
    
    @Override
    public RecordCounter readFileAndWriteToDb(Path ctlFile, Path datFile,String table) throws Exception {
        RowDataProcessor rowDataProcessor = this.rowProcessorFactory.getRowDataProcessor(table);
        if(rowDataProcessor == null){
            rowProcessorFactory.registerRowDataProcessor(table, poolManager.getPooledConnection(), columnSeparator);
        }
        if(dbWriterQueue == null){
            dbWriterQueue = disruptorFactory.writeDisruptor(writeDbQuqueConfig);
        }
        Disruptor<RowString> queue = tableQueue.get(table);
        if(queue == null){
            queue = disruptorFactory.readDisruptor(readStrQueueConfig, table, dbWriterQueue, poolManager);
            tableQueue.put(table, queue);
        }

        String[] titles = getColumns(ctlFile);
        var writer = queue;
        AtomicLong count = new AtomicLong(0);
        AtomicLong target = new AtomicLong(0);
        Files.lines(datFile, UTF8).filter(line->!line.isBlank()).forEach(row -> {
            count.getAndIncrement();
            writer.publishEvent((event, sequence) -> {
                event.setRow(row);
                event.setColums(titles);
                event.setCounter(target);
            });
        });
        
        return new RecordCounter(count.get(), target);
    }

    @Override
    public void registerRowDataProcessor(String tableName) throws Exception {
        Connection connection = poolManager.getPooledConnection();
        rowProcessorFactory.registerRowDataProcessor(tableName,connection, columnSeparator);
        connection.close();
    }
    
    private String[] getColumns(Path ctlFile) throws Exception{
        return Files.lines(ctlFile, UTF8).filter(row -> !row.trim().isBlank()).findFirst().get().split(Pattern.quote(columnSeparator));
    }
}
