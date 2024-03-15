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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-16  00:28
 *
 */
public class TableCounter {
    
    private final static ConcurrentHashMap<String,AtomicLong> COUNTER_MAP = new ConcurrentHashMap<>();
    
    public static long incrementAndGet(String tableName,long addtion){
        AtomicLong counter = COUNTER_MAP.get(tableName);
        if(counter==null){
            counter = new AtomicLong(0);
            AtomicLong oldCounter = COUNTER_MAP.putIfAbsent(tableName, counter);
            if(oldCounter!=null){
                counter = oldCounter;
            }
        }
        return counter.addAndGet(addtion);
    }
    
    public static long get(String tableName){
        AtomicLong counter = COUNTER_MAP.get(tableName);
        if(counter==null){
            counter = new AtomicLong(0);
            AtomicLong oldCounter = COUNTER_MAP.putIfAbsent(tableName, counter);
            if(oldCounter!=null){
                counter = oldCounter;
            }
        }
        return counter.get();
    }
    
    public static long reset(String tableName){
        AtomicLong counter = COUNTER_MAP.get(tableName);
        if(counter==null){
            counter = new AtomicLong(0);
            AtomicLong oldCounter = COUNTER_MAP.putIfAbsent(tableName, counter);
            if(oldCounter!=null){
                counter = oldCounter;
            }
        }
        return counter.getAndSet(0);
    }
}
