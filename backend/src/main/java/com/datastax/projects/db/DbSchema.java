package com.datastax.projects.db;

import com.datastax.dse.driver.api.core.graph.GraphNode;
import com.datastax.dse.driver.api.core.graph.GraphResultSet;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSchema {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbSchema.class);


    public static final String SCHEMA =
            "schema.type('diagnosis').ifNotExists().property('infectious_from', Timestamp).property('infectious_until', Timestamp).property('diagnosed_by', Varchar).property('diagnosed_on', Timestamp).create()\n" +
            "schema.type('testresult').ifNotExists().property('time_taken', Timestamp).property('time_result', Timestamp).property('result', Boolean).create()\n" +

            "schema.vertexLabel('device').ifNotExists().partitionBy('device_id', Varchar).property('last_sync', Timestamp).create()\n" +
            "schema.vertexLabel('person').ifNotExists().partitionBy('person_id', Uuid).property('diagnoses', listOf(frozen(typeOf('diagnosis')))).property('tests', listOf(frozen(typeOf('testresult')))).create()\n" +

            "schema.edgeLabel('contact').tableName('device_contact').ifNotExists().from('device').to('device').partitionBy(OUT, 'device_id', 'device1_id').clusterBy('time', Timestamp, Desc).clusterBy(IN, 'device_id', 'device2_id', Asc).property('duration_sec', Int).create()\n" +
            "schema.edgeLabel('contact').from('device').to('device').materializedView('device_contact_inverse').ifNotExists().partitionBy(IN, 'device_id').clusterBy('time', Desc).clusterBy(OUT, 'device_id', Asc).create()\n" +
            "schema.edgeLabel('own').tableName('person_owns_device').ifNotExists().from('person').to('device').partitionBy(OUT, 'person_id', 'person_id').clusterBy(IN, 'device_id', 'device_id', Asc).property('claimed_on', Timestamp).create()\n" +
            "schema.edgeLabel('own').from('person').to('device').materializedView('device_ownedby_person').ifNotExists().inverse().create()"
            ;

    public static final String CREATE_KEYSPACE =
            "CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1 } AND graph_engine = 'Core';";

    private static void executeDDL(String ddl, CqlSession session) {
        try {
            LOGGER.info("Executing schema DDL: {}", ddl);
            ResultSet rs = session.execute(SimpleStatement.newInstance(ddl).setConsistencyLevel(DefaultConsistencyLevel.ALL));
            for (Row row : rs) {
                LOGGER.debug("Schema DDL result: {}", row);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error during schema creation query.", ex);
        }
    }

    private static void executeGraphDDL(String ddl, String keyspace, CqlSession session) {
        try {
            LOGGER.info("Executing schema DDL: {}", ddl);
            ScriptGraphStatement stmt = ScriptGraphStatement.newInstance(ddl).setGraphName(keyspace).setConsistencyLevel(DefaultConsistencyLevel.ALL);
            GraphResultSet rs = session.execute(stmt);
            for (GraphNode row : rs) {
                LOGGER.debug("Schema DDL result: {}", row);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error during schema creation query.", ex);
        }
    }

    public static void createSchema(String keyspace, CqlSession session) {
        executeDDL(String.format(CREATE_KEYSPACE,keyspace), session);

        executeGraphDDL(SCHEMA,keyspace,session);

        try {
            int checkTry = 0;
            while (true) {
                // should automatically be done under the hood as soon as a DDL statement is executed, but better ensure it
                if (session.checkSchemaAgreement()) {
                    break;
                } else {
                    Thread.sleep(2000);
                }

                if (++checkTry > 15) {
                    throw new RuntimeException("All the nodes in the database did not agree on a common " +
                            "schema within 30 seconds. AppStax would run in degraded conditions in this situation.");
                }
            }
        } catch (InterruptedException e) {
            if (!session.checkSchemaAgreement()) {
                throw new RuntimeException("Stopped while doing schema agreement checks and schema is not ready.");
            }
        }

        // final schema refresh
        session.refreshSchema();
    }

}
