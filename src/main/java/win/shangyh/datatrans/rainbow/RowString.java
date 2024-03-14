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
package win.shangyh.datatrans.rainbow;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-02-26  16:09
 *
 */
public class RowString {

    private String[] colums;
    
    private String row;
    
    private AtomicLong counter;
    
    private boolean endStage=false;
    
    private boolean posioned=false;
    
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
    
    public AtomicLong getCounter() {
        return counter;
    }
    
    public void setCounter(AtomicLong counter) {
        this.counter = counter;
    }
    
    public boolean isEndStage() {
        return endStage;
    }
    
    public void setEndStage() {
        this.endStage = true;
    }
    
    public void clear() {
        row = null;
        colums = null;
        counter = null;
        endStage = false;
    }
}
