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
package win.shangyh.datatrans.rainbow.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lmax.disruptor.dsl.Disruptor;

import win.shangyh.datatrans.rainbow.data.RowString;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-16  02:03
 *
 */
public class QueueRegister {
    
    private final static Map<String, Disruptor<RowString>> QUEUE_MAP = new ConcurrentHashMap<>();
    
    public static void registerQueue(String table, Disruptor<RowString> queue){
        QUEUE_MAP.put(table, queue);
    }
    
    public static Disruptor<RowString> getQueue(String table){
        return QUEUE_MAP.get(table);
    }

    public static void unregisterQueue(String tableName) {
        QUEUE_MAP.remove(tableName);
    }
}
