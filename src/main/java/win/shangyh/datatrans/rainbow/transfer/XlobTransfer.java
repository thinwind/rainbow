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
 * @since 2024-03-06  21:54
 *
 */
public class XlobTransfer  implements ColumnTransfer<byte[]>{
    
    static {
        XlobTransfer xlobTransfer = new XlobTransfer();
        ColumnTransferRegister.registerColumnTransfer(java.sql.Types.BLOB, xlobTransfer);
        ColumnTransferRegister.registerColumnTransfer(java.sql.Types.CLOB, xlobTransfer);
    }

    @Override
    public byte[] transferFromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.getBytes();
    }

    @Override
    public String transferToString(byte[] value) {
        if (value == null) {
            return null;
        }
        return new String(value);
    }
    
}
