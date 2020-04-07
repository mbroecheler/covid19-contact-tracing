package com.datastax.projects.resources;

import com.codahale.metrics.annotation.Timed;
import com.datastax.dse.driver.api.core.graph.GraphNode;
import com.datastax.dse.driver.api.core.graph.GraphResultSet;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.projects.api.Contact;
import com.datastax.projects.db.DbSessionClientManager;
import com.google.common.base.Preconditions;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.datastax.dse.driver.api.core.graph.DseGraph.g;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;

@Path("/tracer/")
@Produces(MediaType.APPLICATION_JSON)
public class ContactTracingResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactTracingResource.class);


    private final DbSessionClientManager sessionManager;
    private final int infectionWindowHours;


    public ContactTracingResource(DbSessionClientManager sessionManager, int infectionWindowHours) {
        this.sessionManager = sessionManager;
        Preconditions.checkArgument(infectionWindowHours>0 && infectionWindowHours<=24*14,
                "Invalid infection window length specified: %s",infectionWindowHours);
        this.infectionWindowHours = infectionWindowHours;
    }

    @GET
    @Path("{person_id}/infected")
    @Timed
    public List<Contact> trace(@PathParam("person_id") @NotNull UUID person_id, @QueryParam("from") Optional<String> infectious) {
        Preconditions.checkNotNull(person_id);
        final Instant from;
        if (infectious.isPresent()) {
            try {
                from = Instant.parse(infectious.get());
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Invalid date provided: ["+infectious.get()+"]. Expected something like [2016-08-18T06:17:10.225Z]");
            }
        } else {
            from = Instant.now().minus(infectionWindowHours,ChronoUnit.HOURS);
        }

        GraphResultSet results = sessionManager.executeGraphTraversal(tracingQuery(person_id,from));
        LOGGER.info("Executed contact tracing query. Errors: {}", results.getRequestExecutionInfo().getErrors().toString());

        List<ContactTracker> contacts = resultToTrackers(results);
        LOGGER.info("Contacts size: {}", contacts.size());
        return contacts.stream().map(c -> c.toContact()).collect(Collectors.toList());
    }

    /**
     * Returns a graph traversal which finds all other individuals who had a contact with the given individual {@code person_id}
     * via any device after the given time point {@code infectious_from}.
     * For each such contact, it returns the time it occured and the duration. The results are ordered by contacted
     * individual first and time of contact secondarily.
     *
     * @param person_id
     * @param infectious_from
     * @return
     */
    private GraphTraversal tracingQuery(UUID person_id, Instant infectious_from) {
        return g.V().has("person","person_id",person_id).out("own")
                .bothE("contact").has("time", P.gte(infectious_from)).as("contact").otherV().in("own")
                .order().by("person_id", Order.asc).by(select("contact").values("time"),Order.asc)
                .project("person_id","duration","time").
                        by(values("person_id")).
                        by(select("contact").values("duration_sec")).
                        by(select("contact").values("time"));
    }

    /**
     * Parses the result set from the {@link #tracingQuery(UUID, Instant)} into a list of {@link ContactTracker}s
     * that consolidate multiple contacts to the same individual by summing up the duration and removes duplicate contact
     * records that are likely to occur when both devices submit a contact record.
     *
     * @param results
     * @return
     */
    private List<ContactTracker> resultToTrackers(GraphResultSet results)  {
        List<ContactTracker> contacts = new ArrayList<>();
        ContactTracker current = null;
        for (GraphNode result : results) {
            LOGGER.info("Result: {}", result.toString());
            Map<String,Object> res = result.asMap();

            if (current==null) {
                //First Person
                current = ContactTracker.fromResult(res);
            } else if (!current.person_id.equals(res.get("person_id"))) {
                //Next Person
                contacts.add(current);
                current = ContactTracker.fromResult(res);
            } else {
                //Add Contact to current person tracker
                current.addContact(res);
            }
        }
        if (current!=null) contacts.add(current);
        return contacts;
    }

    /**
     * Helper class to consolidate contact records to another person
     */
    private static class ContactTracker {

        final UUID person_id;
        final Instant first_contact;
        int total_duration;

        Instant lastContact;
        int lastDuration;

        ContactTracker(UUID person_id, Instant time, int duration) {
            this.person_id = person_id;
            this.first_contact = time;
            this.total_duration = duration;

            lastContact = time;
            lastDuration = duration;
        }

        void addContact(Map<String,Object> result) {
            Preconditions.checkNotNull(person_id.equals(result.get("person_id")));
            Instant nextContact = (Instant)result.get("time");
            int nextDuration = (int)result.get("duration");
            Preconditions.checkArgument(lastContact.equals(nextContact) || lastContact.isBefore(nextContact),
                    "Timestamp order is violated");

            //Ignore this record if it happened during the last contact since it is likely a duplicate
            //or recorded by the contacting device (i.e. most contacts are recorded twice since each device may record it)
            if (lastContact.plusSeconds(lastDuration).isBefore(nextContact)) {
                total_duration += nextDuration;

                lastContact = nextContact;
                lastDuration = nextDuration;
            }
        }

        Contact toContact() {
            return new Contact(person_id,first_contact.toString(),total_duration);
        }

        static ContactTracker fromResult(Map<String,Object> result) {
            return new ContactTracker((UUID)result.get("person_id"),
                    (Instant)result.get("time"),
                    (Integer)result.get("duration"));
        }

    }



}
