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

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-16  02:06
 *
 */
public interface QueueService {

    /**
    * 注册行数据处理器
    * @param tableName 表名
    * @throws Exception
    */
    void registerRowDataProcessor(String tableName)
            throws Exception;

    /**
     * 注册读取队列
     * @param tableName 表名
     * @throws Exception
     * @return void
     * @since 2024-03-16  02:06
     */
    public void registerReadQueue(String tableName) throws Exception;
    
    
    /**
     * 注销读取队列
     * @param tableName
     * @throws Exception
     */
    public void unregisterReadQueue(String tableName) throws Exception;
}
