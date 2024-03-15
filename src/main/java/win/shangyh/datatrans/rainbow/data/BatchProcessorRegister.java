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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-15  22:03
 *
 */
public class BatchProcessorRegister {
    
    private final static Map<String, BatchProcessor> BATCHPROCESSOR_MAP=new ConcurrentHashMap<>();
    
    
    public static void registerBatchProcessor(String tableName, BatchProcessor batchProcessor){
        Objects.requireNonNull(tableName,"表名不能为NULL");
        Objects.requireNonNull(batchProcessor,"批量处理器不能为空");
        BATCHPROCESSOR_MAP.put(tableName, batchProcessor);
    }
    
    public static BatchProcessor getBatchProcessor(String tableName){
        return BATCHPROCESSOR_MAP.get(tableName);
    }
}
