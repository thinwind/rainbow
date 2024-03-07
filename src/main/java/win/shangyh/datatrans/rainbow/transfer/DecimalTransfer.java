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

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-07  11:01
 *
 */
public class DecimalTransfer implements ColumnTransfer<Double> {
    
    static {
        DecimalTransfer decimalTransfer = new DecimalTransfer();
        ColumnTransferRegister.registerColumnTransfer(java.sql.Types.DECIMAL, decimalTransfer);
        ColumnTransferRegister.registerColumnTransfer(java.sql.Types.NUMERIC, decimalTransfer);
        ColumnTransferRegister.registerColumnTransfer(java.sql.Types.DOUBLE, decimalTransfer);
        ColumnTransferRegister.registerColumnTransfer(java.sql.Types.FLOAT, decimalTransfer);
        ColumnTransferRegister.registerColumnTransfer(java.sql.Types.REAL, decimalTransfer);
    }

    @Override
    public Double transferFromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Double.valueOf(value);
    }

    @Override
    public String transferToString(Double value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
}
