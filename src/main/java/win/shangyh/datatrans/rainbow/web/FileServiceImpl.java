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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import win.shangyh.datatrans.rainbow.ConnectionPoolManager;
import win.shangyh.datatrans.rainbow.DisruptorFactory;
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
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    @Value("${rb.column.separator}")
    private String columnSeparator;
    
    @Override
    public int readFileAndWriteToDb(Path ctlFile, Path datFile) throws Exception {
        String[] titles = getTitles(ctlFile);
        
        return 0;
    }

    @Override
    public void registerRowDataProcessor(String tableName) throws Exception {
        Connection connection = poolManager.getPooledConnection();
        rowProcessorFactory.registerRowDataProcessor(tableName,connection, columnSeparator);
        connection.close();
    }
    
    private String[] getTitles(Path ctlFile) throws Exception{
        return Files.lines(ctlFile, UTF8).filter(row -> row.trim().isBlank()).findFirst().get().split(columnSeparator);
    }
}
