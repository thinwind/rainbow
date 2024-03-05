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

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-04  19:11
 *
 */
public final class ColumnTransferRegister {
    
    private ColumnTransferRegister() {
    }

    private final static ConcurrentHashMap<Integer, ColumnTransfer> COLUMN_TRANSFER_BOX = new ConcurrentHashMap<>();

    public static ColumnTransfer getColumnTransfer(int columnType) {
        return COLUMN_TRANSFER_BOX.get(columnType);
    }

    public static void registerColumnTransfer(int columnType, ColumnTransfer columnTransfer) {
        COLUMN_TRANSFER_BOX.put(columnType, columnTransfer);
    }
}
