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

import java.nio.file.Path;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-09  08:42
 *
 */
public interface FileService {

    
    /**
     * 读取文件并写入数据库
     * @param ctlFile ctl文件
     * @param datFile dat文件
     * @return 写入数据库的记录数
     * @throws Exception
     */
    int readFileAndWriteToDb(Path ctlFile, Path datFile) throws Exception;

    /**
     * 注册行数据处理器
     * @param tableName 表名
     * @throws Exception
     */
    void registerRowDataProcessor(String tableName) throws Exception;
    
}
