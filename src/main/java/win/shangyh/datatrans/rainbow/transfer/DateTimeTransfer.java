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

import java.time.LocalDateTime;

import win.shangyh.datatrans.rainbow.util.DateUtil;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-06  21:52
 *
 */
public class DateTimeTransfer implements ColumnTransfer<LocalDateTime>{
    
    private final DateUtil dateUtil;
    
    public DateTimeTransfer(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }
    
    @Override
    public LocalDateTime transferFromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return dateUtil.parseDateTime(value);
    }
    
    @Override
    public String transferToString(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return dateUtil.formatDateTime(value);
    }
}
