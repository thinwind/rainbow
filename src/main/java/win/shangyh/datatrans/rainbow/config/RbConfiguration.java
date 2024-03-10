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
package win.shangyh.datatrans.rainbow.config;

import java.sql.Types;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import win.shangyh.datatrans.rainbow.ConnectionPoolManager;
import win.shangyh.datatrans.rainbow.DatabaseInfo;
import win.shangyh.datatrans.rainbow.DisruptorFactory;
import win.shangyh.datatrans.rainbow.processor.RowProcessorFactory;
import win.shangyh.datatrans.rainbow.processor.RowProcessorFactoryImpl;
import win.shangyh.datatrans.rainbow.transfer.ColumnTransferRegister;
import win.shangyh.datatrans.rainbow.transfer.DateTimeTransfer;
import win.shangyh.datatrans.rainbow.transfer.DateTransfer;
import win.shangyh.datatrans.rainbow.transfer.TimeTransfer;
import win.shangyh.datatrans.rainbow.util.DateUtil;

/**
 *
 * 应用配置
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-02  18:00
 *
 */
@Configuration
public class RbConfiguration {
    
    @Bean
    public DatabaseInfo databaseInfo(@Value("${rb.database.url}") String url,
                @Value("${rb.database.username}") String user,
                @Value("${rb.database.password}") String pwd){
        return new DatabaseInfo(url, user, pwd);
    }
    
    @Bean
    public DateUtil dateUtil(@Value("${rb.date.dateformat}") String dateFormat,
                @Value("${rb.date.datetimeformat}") String dateTimeFormat,
                @Value("${rb.date.timeformat}") String timeFormat){
        return new DateUtil(dateFormat, dateTimeFormat, timeFormat);
    }
    
    @Bean
    public RowProcessorFactory rowProcessorFactory(){
        return new RowProcessorFactoryImpl();
    }
    
    @Bean
    public DisruptorFactory disruptorFactory(){
        return new DisruptorFactory(rowProcessorFactory());
    }
    
    @Bean
    public ConnectionPoolManager connectionPoolManager(DatabaseInfo databaseInfo,@Value("${rb.database.maxconn}") int maxConnCount){
        return new ConnectionPoolManager(databaseInfo,maxConnCount);
    }
    
}
