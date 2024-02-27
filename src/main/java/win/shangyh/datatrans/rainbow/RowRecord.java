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
package win.shangyh.datatrans.rainbow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-02-26  16:12
 *
 */
public class RowRecord {

    private String tableName;

    private String[] rowTitles;

    private Object[] rowValues;

    Connection connection;

    PreparedStatement preparedStatement;

    public void prepare() throws SQLException{
        StringBuilder builder=new StringBuilder();
        builder.append("insert into ").append(tableName).append(" (");
        Object[] params = new Object[rowValues.length];
        int idx=0;
        
        for (int i = 0; i < rowValues.length; i++) {
            if(rowValues[i]!=null){
                builder.append(rowTitles[i]).append(",");
                params[idx++]=rowValues[i];
            }
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(") values (");
        for (int i = 0; i < idx; i++) {
            builder.append("?,");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        
        preparedStatement = connection.prepareStatement(builder.toString());
        for (int i = 0; i < idx; i++) {
            preparedStatement.setObject(i+1, params[i]);
        }
        
    }

    public void clear() {
        rowTitles = null;
        rowValues = null;
        tableName = null;
        connection = null;
        preparedStatement = null;
    }
}
