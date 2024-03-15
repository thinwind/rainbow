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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import win.shangyh.datatrans.rainbow.connection.RainbowPool;
import win.shangyh.datatrans.rainbow.data.BatchProcessor;
import win.shangyh.datatrans.rainbow.data.BatchProcessorRegister;
import win.shangyh.datatrans.rainbow.data.LineCounter;
import win.shangyh.datatrans.rainbow.data.TableCounter;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-08  22:32
 *
 */
@RestController
@RequestMapping("/load")
public class LoadController {

    //logger
    private final static Logger logger = LoggerFactory.getLogger(LoadController.class);

    @Value("${rb.file.in.dir}")
    private String loadBaseDir;

    @Value("${rb.file.ctl}")
    private String ctlSuffix;

    @Value("${rb.file.dat}")
    private String datSuffix;

    @Autowired
    private FileService fileService;

    private final static String TOKEN = "RtlTbLd2024";

    @Autowired
    RainbowPool poolManager;

    @Autowired
    QueueService queueService;

    // private final Charset charset = Charset.forName("UTF-8");

    @PostMapping("/{tableName}")
    public Object loadTable(@PathVariable String tableName, String token) {
        if (!TOKEN.equals(token)) {
            return "Token error!";
        }

        Map<String, Object> result = new HashMap<>();

        Path ctlFile = Paths.get(loadBaseDir, tableName + ctlSuffix);
        if (!Files.exists(ctlFile)) {
            result.put("success", false);
            result.put("message", "Table " + tableName + ctlSuffix + " not exists!");
            return result;
        }

        Path datFile = Paths.get(loadBaseDir, tableName + datSuffix);
        if (!Files.exists(datFile)) {
            result.put("success", false);
            result.put("message", "Table " + tableName + datSuffix + " not exists!");
            return result;
        }

        try {
            // fileService.registerRowDataProcessor(tableName);
            queueService.registerRowDataProcessor(tableName);
            queueService.registerReadQueue(tableName);
        } catch (Exception e) {
            logger.error("注册行数据处理器错误", e);
            result.put("success", false);
            result.put("message", e.toString());
            return result;
        }

        try {
            LineCounter.reset(tableName);
            TableCounter.reset(tableName);

            long start = System.currentTimeMillis();
            long recCnt = fileService.readFileAndWriteToDb(ctlFile, datFile, tableName);
            while (recCnt > LineCounter.get(tableName)) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    logger.error("等待加载文件完成错误", e);
                }
            }

            BatchProcessor batchProcessor = BatchProcessorRegister.getBatchProcessor(tableName);

            do {
                try {
                    batchProcessor.flush();
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    logger.error("等待加载文件完成错误", e);
                }
            } while (recCnt > TableCounter.get(tableName));

            long end = System.currentTimeMillis();
            result.put("success", true);
            result.put("message",
                    "Load table " + tableName + " finished. " + recCnt + " records loaded.");
            result.put("time_expense", end - start);
            return result;
        } catch (Exception e) {
            logger.error("加载文件错误", e);
            result.put("success", false);
            result.put("message", e.toString());
            return result;
        }
    }
}
