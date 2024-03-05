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
package win.shangyh.datatrans.rainbow.transfer;

import java.sql.Types;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-05  10:17
 *
 */
public class VarcharTransfer implements ColumnTransfer{
    
    static {
        VarcharTransfer varcharTransfer = new VarcharTransfer();
        ColumnTransferRegister.registerColumnTransfer(Types.CHAR, varcharTransfer);
        ColumnTransferRegister.registerColumnTransfer(Types.VARCHAR, varcharTransfer);
        ColumnTransferRegister.registerColumnTransfer(Types.LONGVARCHAR, varcharTransfer);
        ColumnTransferRegister.registerColumnTransfer(Types.NCHAR, varcharTransfer);
        ColumnTransferRegister.registerColumnTransfer(Types.NVARCHAR, varcharTransfer);
        ColumnTransferRegister.registerColumnTransfer(Types.LONGNVARCHAR, varcharTransfer);
    }

    @Override
    public Object transferFromString(String value) {
        return value;
    }

    @Override
    public String transferToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
}
