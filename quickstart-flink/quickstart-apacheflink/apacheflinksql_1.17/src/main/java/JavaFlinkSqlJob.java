/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.StatementSet;
import org.apache.flink.table.api.TableEnvironment;

public class JavaFlinkSqlJob {
    public static void main(String[] args) {
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .inBatchMode()
                .build();

        TableEnvironment tableEnvironment = TableEnvironment.create(settings);

        tableEnvironment.executeSql("CREATE TABLE datagen (\n" +
                "f_sequence INT,\n" +
                "f_random INT,\n" +
                "f_random_str STRING,\n" +
                "ts AS localtimestamp,\n" +
                "WATERMARK FOR ts AS ts\n" +
                ") WITH (\n" +
                "'connector' = 'datagen',\n" +
                "'rows-per-second'='5',\n" +
                "'fields.f_sequence.kind'='sequence',\n" +
                "'fields.f_sequence.start'='1',\n" +
                "'fields.f_sequence.end'='100',\n" +
                "'fields.f_random.min'='1',\n" +
                "'fields.f_random.max'='100',\n" +
                "'fields.f_random_str.length'='10',\n" +
                "'number-of-rows'='10000'\n" +
                ")");

        tableEnvironment.executeSql("CREATE TABLE print_table (\n" +
                "f_sequence INT,\n" +
                "f_random INT,\n" +
                "f_random_str STRING\n" +
                ") WITH (\n" +
                "'connector' = 'print'\n" +
                ")");

        StatementSet statementSet = tableEnvironment.createStatementSet();
        statementSet.addInsertSql("INSERT INTO print_table select f_sequence,f_random,f_random_str from datagen");

        statementSet.execute().print();
    }
}
