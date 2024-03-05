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

import java.util.Objects;

import win.shangyh.datatrans.rainbow.transfer.ColumnTransfer;
import win.shangyh.datatrans.rainbow.transfer.ColumnTransferRegister;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-02  18:02
 *
 */
public class DefaultRowDataProceessorImpl implements RowDataProcessor{
    
    private final String tableName;
    
    private final String[] rowTitles;
    
    private final int[] columnTypes;
    
    private final String fieldSeparator;
    
    public DefaultRowDataProceessorImpl(String tableName, String[] rowTitles, String fieldSeparator, int[] columnTypes) {
        Objects.requireNonNull(tableName, "tableName must not be null");
        Objects.requireNonNull(rowTitles, "rowTitles must not be null");
        Objects.requireNonNull(fieldSeparator, "fieldSeparator must not be null");
        Objects.requireNonNull(columnTypes, "columnTypes must not be null");
        this.tableName = tableName;
        this.rowTitles = rowTitles;
        this.columnTypes = columnTypes;
        this.fieldSeparator = fieldSeparator;
    }

    @Override
    public Object[] parseRow(String row) {
        String[] fields = row.split(fieldSeparator);
        Object[] result = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            result[i] = parseField(fields[i], columnTypes[i]);
        }
        return result;
    }

    private Object parseField(String val, int columnType) {
        ColumnTransfer columnTransfer = ColumnTransferRegister.getColumnTransfer(columnType);
        return columnTransfer.transferFromString(val);
    }

    @Override
    public String[] getRowTitles() {
        return rowTitles;
    }

    @Override
    public String getTableName() {
        return tableName;
    }
    
}
