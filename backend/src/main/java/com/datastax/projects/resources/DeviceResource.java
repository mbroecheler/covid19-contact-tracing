package com.datastax.projects.resources;


import com.codahale.metrics.annotation.Timed;
import com.datastax.dse.driver.api.core.graph.GraphResultSet;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.projects.api.Contact;
import com.datastax.projects.api.DeviceClaim;
import com.datastax.projects.api.DeviceContact;
import com.datastax.projects.db.DbSessionClientManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.datastax.dse.driver.api.core.graph.DseGraph.g;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;

@Path("/device/")
@Produces(MediaType.APPLICATION_JSON)
public class DeviceResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceResource.class);

    private final DbSessionClientManager sessionManager;

    public DeviceResource(DbSessionClientManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private static final String CLAIM_GREMLIN =
            "v1 = g.addV('person').property('person_id', person_id).next()\n" +
                    "v2 = g.addV('device').property('device_id', device_id).next()\n" +
                    "g.addE('own').from(v1).to(v2).property('claimed_on',time)";

    @POST
    @Path("{device_id}/claim")
    @Timed
    public String claim(@PathParam("device_id") @NotEmpty String device_id, @NotNull DeviceClaim claim) {
        UUID person_id = claim.getPerson_id();
        Instant time = Instant.now();
        Preconditions.checkNotNull(person_id);

        GraphResultSet results = sessionManager.executeGraphTraversal(CLAIM_GREMLIN,
                ImmutableMap.of("person_id", person_id, "device_id", device_id, "time", time));
        LOGGER.info("Executed contact tracing query. Errors: {}", results.getRequestExecutionInfo().getErrors().toString());

        return "Success";
    }

    private static final String INSERT_CONTACT_QUERY =
            "INSERT INTO \"%s\".device_contact (device1_id, device2_id, time, duration_sec) VALUES (:id1, :id2, :time, :duration)";
    private PreparedStatement INSERT_CONTACT = null;

    @POST
    @Path("{device_id}/contacts")
    @Timed
    public String contacts(@PathParam("device_id") @NotEmpty String device_id, @NotNull List<DeviceContact> contacts) {
        Preconditions.checkArgument(!contacts.isEmpty());

        //Initialize insert
        if (INSERT_CONTACT == null) {
            synchronized (this) {
                if (INSERT_CONTACT == null) {
                    INSERT_CONTACT = sessionManager.prepare(INSERT_CONTACT_QUERY);
                }
            }
        }

        BatchStatementBuilder batch = BatchStatement.builder(DefaultBatchType.UNLOGGED);
        for (DeviceContact contact : contacts) {
            batch = batch.addStatement(INSERT_CONTACT.bind(device_id, contact.getOther_id(), contact.getTimestamp(),
                    contact.getDuration()));
        }
        LOGGER.info("Executing contact batch with # elements = {}", contacts.size());
        ResultSet results = sessionManager.executeBatch(batch.build());
        LOGGER.info("Executed contact tracing query. Errors: {}", results.getExecutionInfo().getErrors().toString());


        return "Success";
    }




}
