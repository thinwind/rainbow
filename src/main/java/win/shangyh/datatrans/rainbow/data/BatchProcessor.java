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

import java.util.Objects;

import com.lmax.disruptor.dsl.Disruptor;

/**
 *
 * TODO 说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-15  16:56
 *
 */
public class BatchProcessor {

    private final Object[] locks;

    private final int[] batchIdx;

    private final int queueSize;

    private final int batchSize;

    /**
     * 三维数组，第一维是锁，第二维批量，第三维是行数据
     */
    private final Object[][][] valueHolders;

    private final Disruptor<RowRecord> disruptor;

    private final String insertSql;

    private final String tableName;

    private final int[] columnTypes;

    private BatchProcessor(int queueSize, int batchSize, Disruptor<RowRecord> disruptor, String insertSql,
            int[] columnTypes, String tableName) {
        this.queueSize = queueSize;
        this.batchSize = batchSize;
        this.disruptor = disruptor;
        this.insertSql = insertSql;
        this.tableName = tableName;
        this.columnTypes = columnTypes;
        locks = initLocks();
        valueHolders = initValueHolders();
        batchIdx = initBatchIdx();
    }

    //Builder模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int queueSize;
        private int batchSize;
        private Disruptor<RowRecord> disruptor;
        private String insertSql;
        private int[] columnTypes;
        private String tableName;

        public Builder queueSize(int queueSize) {
            this.queueSize = queueSize;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder disruptor(Disruptor<RowRecord> disruptor) {
            this.disruptor = disruptor;
            return this;
        }

        public Builder insertSql(String insertSql) {
            this.insertSql = insertSql;
            return this;
        }

        public Builder columnTypes(int[] columnTypes) {
            this.columnTypes = columnTypes;
            return this;
        }

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public BatchProcessor build() {
            Objects.requireNonNull(disruptor, "disruptor must not be null");
            Objects.requireNonNull(insertSql, "insertSql must not be null");
            Objects.requireNonNull(columnTypes, "columnTypes must not be null");
            Objects.requireNonNull(tableName, "tableName must not be null");
            return new BatchProcessor(queueSize, batchSize, disruptor, insertSql, columnTypes, tableName);
        }
    }

    private int[] initBatchIdx() {
        int[] r = new int[queueSize];
        for (int i = 0; i < queueSize; i++) {
            r[i] = 0;
        }
        return r;
    }

    public void addRow(int queue, Object[] row) {
        int queuePos = queue % queueSize;
        synchronized (locks[queuePos]) {
            // int batch = batchIdx[queuePos];
            valueHolders[queuePos][batchIdx[queuePos]++] = row;
            if (batchIdx[queuePos] == batchSize) {
                //提交到db
                disruptor.publishEvent((event, sequence) -> {
                    event.setInsertSql(insertSql);
                    event.setColumnTypes(columnTypes);
                    event.setValues(valueHolders[queuePos]);
                    event.setTableName(tableName);
                });
                valueHolders[queuePos] = new Object[batchSize][];
                batchIdx[queuePos] = 0;
            }
        }
    }

    public void flush() {
        for (int i = 0; i < queueSize; i++) {
            synchronized (locks[i]) {
                // int batch = batchIdx[i];
                if (batchIdx[i] > 0) {
                    int j = i;
                    disruptor.publishEvent((event, sequence) -> {
                        event.setInsertSql(insertSql);
                        event.setColumnTypes(columnTypes);
                        event.setValues(valueHolders[j]);
                        event.setTableName(tableName);
                    });
                    valueHolders[i] = new Object[batchSize][];
                    batchIdx[i]=0;
                }
            }
        }
    }

    private Object[][][] initValueHolders() {
        return new Object[queueSize][batchSize][];
    }

    private Object[] initLocks() {
        var q = new Object[queueSize];
        for (int i = 0; i < queueSize; i++) {
            q[i] = new Object();
        }
        return q;
    }
}
