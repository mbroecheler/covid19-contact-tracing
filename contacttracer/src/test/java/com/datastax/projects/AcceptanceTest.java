package com.datastax.projects;


import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.UUID;

public class AcceptanceTest {

    public static final DropwizardTestSupport<ContactTracerConfiguration> SUPPORT =
            new DropwizardTestSupport<ContactTracerConfiguration>(ContactTracerApplication.class,
                    ResourceHelpers.resourceFilePath("test-config.yml")
            );

    @BeforeAll
    public static void beforeClass() throws Exception {
        SUPPORT.before();
        Thread.sleep(1000); //generous wait for db connection
    }

    @AfterAll
    public static void afterClass() {
        SUPPORT.after();
    }

    @Test
    public void basicResponseCheck() {
        Client client = new JerseyClientBuilder(SUPPORT.getEnvironment()).build("test client");

        UUID person_id = UUID.fromString("f11177d2-ec63-3995-bb4a-c628e0d782df");
        Instant from = Instant.now().minusSeconds(3600*24*14);

        Response response = client.target(
                String.format("http://localhost:%d/tracer/%s/infected", SUPPORT.getLocalPort(), person_id.toString()))
                .queryParam("from", from.toString())
                .request()
                .get();

        assertEquals(response.getStatus(),200);

        response.close();

    }

}
