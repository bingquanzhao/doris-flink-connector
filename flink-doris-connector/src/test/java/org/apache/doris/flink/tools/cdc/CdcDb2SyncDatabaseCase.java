// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.flink.tools.cdc;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.doris.flink.tools.cdc.db2.Db2DatabaseSync;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CdcDb2SyncDatabaseCase {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.disableOperatorChaining();
        env.enableCheckpointing(10000);

        //  Map<String,String> flinkMap = new HashMap<>();
        //  flinkMap.put("execution.checkpointing.interval","10s");
        //  flinkMap.put("pipeline.operator-chaining","false");
        //  flinkMap.put("parallelism.default","1");

        //  Configuration configuration = Configuration.fromMap(flinkMap);
        //  env.configure(configuration);

        String database = "db2_test";
        String tablePrefix = "";
        String tableSuffix = "";
        Map<String, String> sourceConfig = new HashMap<>();
        sourceConfig.put("database-name", "testdb");
        sourceConfig.put("schema-name", "DB2INST1");
        sourceConfig.put("hostname", "127.0.0.1");
        sourceConfig.put("port", "50000");
        sourceConfig.put("username", "db2inst1");
        sourceConfig.put("password", "=doris123456");
        // sourceConfig.put("debezium.database.tablename.case.insensitive","false");
        sourceConfig.put("scan.incremental.snapshot.enabled", "true");
        // sourceConfig.put("debezium.include.schema.changes","false");

        Configuration config = Configuration.fromMap(sourceConfig);

        Map<String, String> sinkConfig = new HashMap<>();
        sinkConfig.put("fenodes", "127.0.0.1:8030");
        // sinkConfig.put("benodes","10.20.30.1:8040, 10.20.30.2:8040, 10.20.30.3:8040");
        sinkConfig.put("username", "root");
        sinkConfig.put("password", "123456");
        sinkConfig.put("jdbc-url", "jdbc:mysql://127.0.0.1:9030");
        sinkConfig.put("sink.label-prefix", UUID.randomUUID().toString());
        Configuration sinkConf = Configuration.fromMap(sinkConfig);

        Map<String, String> tableConfig = new HashMap<>();
        tableConfig.put("replication_num", "1");
        //        tableConfig.put("table-buckets", "tbl1:10,tbl2:20,a.*:30,b.*:40,.*:50");
        String includingTables = "FULL_TYPES";
        String excludingTables = null;
        String multiToOneOrigin = null;
        String multiToOneTarget = null;
        boolean ignoreDefaultValue = false;
        boolean useNewSchemaChange = true;
        boolean singleSink = false;
        boolean ignoreIncompatible = false;
        DatabaseSync databaseSync = new Db2DatabaseSync();
        databaseSync
                .setEnv(env)
                .setDatabase(database)
                .setConfig(config)
                .setTablePrefix(tablePrefix)
                .setTableSuffix(tableSuffix)
                .setIncludingTables(includingTables)
                .setExcludingTables(excludingTables)
                .setMultiToOneOrigin(multiToOneOrigin)
                .setMultiToOneTarget(multiToOneTarget)
                .setIgnoreDefaultValue(ignoreDefaultValue)
                .setSinkConfig(sinkConf)
                .setTableConfig(tableConfig)
                .setCreateTableOnly(false)
                .setNewSchemaChange(useNewSchemaChange)
                .setSingleSink(singleSink)
                .setIgnoreIncompatible(ignoreIncompatible)
                .create();
        databaseSync.build();
        env.execute(String.format("DB2-Doris Database Sync: %s", database));
    }
}
