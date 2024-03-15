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

import com.lmax.disruptor.dsl.Disruptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import win.shangyh.datatrans.rainbow.connection.DatabaseInfo;
import win.shangyh.datatrans.rainbow.connection.RainbowPool;
import win.shangyh.datatrans.rainbow.data.RowRecord;
import win.shangyh.datatrans.rainbow.processor.RowProcessorFactory;
import win.shangyh.datatrans.rainbow.processor.RowProcessorFactoryImpl;
import win.shangyh.datatrans.rainbow.queue.QueueFactory;
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
    public QueueFactory disruptorFactory(){
        return new QueueFactory(rowProcessorFactory());
    }
    
    @Bean
    public RainbowPool connectionPoolManager(DatabaseInfo databaseInfo,@Value("${rb.database.maxconn}") int maxConnCount){
        return new RainbowPool(databaseInfo,maxConnCount);
    }
    
    @Bean
    public Disruptor<RowRecord> dbQueue(WriteDbQuqueConfig writeDbQuqueConfig){
        QueueFactory factory = disruptorFactory();
        return factory.writeDbQueue(writeDbQuqueConfig,connectionPoolManager(null,0));
    }
    
}
