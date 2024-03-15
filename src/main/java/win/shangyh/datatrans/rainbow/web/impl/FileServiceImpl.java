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
package win.shangyh.datatrans.rainbow.web.impl;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import win.shangyh.datatrans.rainbow.queue.QueueRegister;
import win.shangyh.datatrans.rainbow.web.FileService;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-09  08:49
 *
 */
@Service
public class FileServiceImpl implements FileService {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Value("${rb.column.separator}")
    private String columnSeparator;

    @Override
    public long readFileAndWriteToDb(Path ctlFile, Path datFile, String table) throws Exception {
        String[] titles = getColumns(ctlFile);
        var writer = QueueRegister.getQueue(table);
        AtomicLong count = new AtomicLong(0);
        Files.lines(datFile, UTF8).filter(line -> !line.isBlank()).forEach(row -> {
            count.getAndIncrement();
            writer.publishEvent((event, sequence) -> {
                event.setRow(row);
                event.setColums(titles);
            });
        });
        return count.get();
    }

    private String[] getColumns(Path ctlFile) throws Exception {
        return Files.lines(ctlFile, UTF8).filter(row -> !row.trim().isBlank()).findFirst().get()
                .split(Pattern.quote(columnSeparator));
    }
}
