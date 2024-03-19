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
package win.shangyh.datatrans.rainbow.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;


/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-11  10:48
 *
 */
public class UnloadController {
    
    @Autowired
    DbService dbService;
    
    @Value("${rb.file.out.dir}")
    private String unloadBaseDir;
    
    private final static String TOKEN="RtlTbUd2024";
    
    @PostMapping("/{tableName}")
    public Object unload(String tableName,String token) {
        if(!TOKEN.equals(token)){
            return "Token error!";
        }
        
        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        dbService.unload(tableName,unloadBaseDir);
        
        long end = System.currentTimeMillis();
        result.put("time", end - start);
        return result;
    }
}
