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
package win.shangyh.datatrans.rainbow.data;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-02-26  16:12
 *
 */
public class RowRecord {
    
    private Object[][] values;

    private String insertSql;

    private int[] columnTypes;
    
    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Object[][] getValues() {
        return values;
    }

    public void setValues(Object[][] values) {
        this.values = values;
    }

    public String getInsertSql() {
        return insertSql;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    public int[] getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(int[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    public void clear() {
        values = null;
        insertSql = null;
        columnTypes = null;
    }
}
