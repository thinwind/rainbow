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
package win.shangyh.datatrans.rainbow.processor;

import java.util.concurrent.ConcurrentHashMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import win.shangyh.datatrans.rainbow.exception.RainbowRuntimeException;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-02  18:28
 *
 */
public class RowProcessorFactoryImpl implements RowProcessorFactory {

    private final static Logger logger = LoggerFactory.getLogger(RowProcessorFactoryImpl.class);

    private final ConcurrentHashMap<String, RowDataProcessor> processorBox = new ConcurrentHashMap<>();

    @Override
    public RowDataProcessor getRowDataProcessor(String tableName) {
        return processorBox.get(tableName);
    }

    @Override
    public void registerRowDataProcessor(String tableName, Connection connection, String fieldSeparator) {
        String sql = "select * from " + tableName + " limit 1";
        ResultSet rs = null;
        try {
            rs = connection.createStatement().executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] rowTitles = new String[columnCount];
            int[] columnTypes = new int[columnCount];
            for (int i = 0; i < columnCount; i++) {
                rowTitles[i] = metaData.getColumnName(i+1);
                columnTypes[i] = metaData.getColumnType(i+1);
            }
            RowDataProcessor rowDataProcessor = new DefaultRowDataProceessorImpl(tableName, rowTitles, fieldSeparator, columnTypes);
            processorBox.put(tableName, rowDataProcessor);
        } catch (SQLException e) {
            logger.error("查询{}表数据错误",tableName, e);
            throw new RainbowRuntimeException(e);
        }
        
    }
}
