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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-01  16:13
 *
 */
@Configuration
@ConfigurationProperties(prefix = "rb.read.queue.file")
public class ReadStrQueueConfig {
    
    private int size;
    
    private int consumer;

    public int getSize() {
        return size;
    }

    public void setSize(int queueSize) {
        this.size = queueSize;
    }

    public int getConsumer() {
        return consumer;
    }

    public void setConsumer(int consumerCount) {
        this.consumer = consumerCount;
    }
}
