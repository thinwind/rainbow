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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

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
public class DefaultRowDataProceessorImpl implements RowDataProcessor {

    private final String tableName;

    private final String[] rowTitles;

    private final int[] columnTypes;

    private final String fieldSeparator;

    private final String orignSep;

    private final Map<String, Integer> columnTypeMap = new HashMap<>();

    private final String insertSql;

    public DefaultRowDataProceessorImpl(String tableName, String[] rowTitles, String fieldSeparator,
            int[] columnTypes) {
        Objects.requireNonNull(tableName, "tableName must not be null");
        Objects.requireNonNull(rowTitles, "rowTitles must not be null");
        Objects.requireNonNull(fieldSeparator, "fieldSeparator must not be null");
        Objects.requireNonNull(columnTypes, "columnTypes must not be null");
        this.tableName = tableName;
        this.rowTitles = rowTitles;
        this.columnTypes = columnTypes;
        this.fieldSeparator = Pattern.quote(fieldSeparator);
        this.orignSep = fieldSeparator;
        this.insertSql = buildInsertSql();
        initColumnTypes();
    }

    private String buildInsertSql() {
        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(tableName).append(" (");
        for (int i = 0; i < rowTitles.length; i++) {
            sb.append(rowTitles[i]);
            if (i < rowTitles.length - 1) {
                sb.append(",");
            }
        }
        sb.append(") values (");
        for (int i = 0; i < rowTitles.length; i++) {
            sb.append("?");
            if (i < rowTitles.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private void initColumnTypes() {
        for (int i = 0; i < columnTypes.length; i++) {
            columnTypeMap.put(rowTitles[i].toLowerCase(), columnTypes[i]);
        }
    }

    @Override
    public Object[] parseRow(String row, String[] colums) {
        String[] fields = row.split(fieldSeparator);
        var valueMap = new HashMap<String, Object>();
        for (int i = 0; i < fields.length; i++) {
            valueMap.put(colums[i], parseField(fields[i], columnTypeMap.get(colums[i])));
        }
        var result = new Object[rowTitles.length];

        for (int i = 0; i < fields.length; i++) {
            result[i] = valueMap.get(rowTitles[i]);
        }
        return result;
    }

    private Object parseField(String val, int columnType) {
        ColumnTransfer<? extends Object> columnTransfer = ColumnTransferRegister.getColumnTransfer(columnType);
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

    @Override
    public String getInsertSql() {
        return insertSql;
    }

    @Override
    public String getColumnSeprator() {
        return orignSep;
    }

    @Override
    public int[] getColumnTypes() {
        return columnTypes;
    }

}
