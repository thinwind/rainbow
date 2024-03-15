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

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-02-26  16:09
 *
 */
public class RowString {
    
    private final static AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    
    public final int id;

    private String[] colums;
    
    private String row;
    
    public RowString() {
        id = ID_GENERATOR.getAndIncrement();
    }
    
    public String getRow() {
        return row;
    }
    
    public void setRow(String row) {
        this.row = row;
    }

    public String[] getColums() {
        return colums;
    }

    public void setColums(String[] colums) {
        this.colums = colums;
    }
    
    public void clear() {
        row = null;
        colums = null;
    }
}
