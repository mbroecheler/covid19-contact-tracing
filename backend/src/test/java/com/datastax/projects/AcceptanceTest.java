package com.datastax.projects;


import com.datastax.projects.api.DeviceClaim;
import com.datastax.projects.api.DeviceContact;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AcceptanceTest {

    public static final DropwizardTestSupport<ContactTracerConfiguration> SUPPORT =
            new DropwizardTestSupport<ContactTracerConfiguration>(ContactTracerApplication.class,
                    ResourceHelpers.resourceFilePath("test-config.yml")
            );

    private static Client client;

    @BeforeAll
    public static void beforeClass() throws Exception {
        SUPPORT.before();
        Thread.sleep(1000); //generous wait for db connection
        client = new JerseyClientBuilder(SUPPORT.getEnvironment()).build("test client");
    }

    @AfterAll
    public static void afterClass() {
        SUPPORT.after();
        client.close();
    }

    @Test
    public void basicTracingCheck() {
        UUID person_id = UUID.fromString("f11177d2-ec63-3995-bb4a-c628e0d782df");
        Instant from = Instant.now().minusSeconds(3600*24*14);

        Response response = client.target(
                String.format("http://localhost:%d/tracer/%s/infected", SUPPORT.getLocalPort(), person_id.toString()))
                .queryParam("from", from.toString())
                .request()
                .get();

        assertEquals(200, response.getStatus());

        response.close();

    }

    private static String getDeviceId(int id) {
        return String.format("%09d", id);
    }

    @Test
    public void basicClaimAndContactsCheck() {
        final String url_prefix = "http://localhost:%d/device/%s/";

        String deviceId = getDeviceId(1);
        UUID person_id = UUID.fromString("f11177d2-ec63-3995-bb4a-c628e0d782df");

        Response response = client.target(
                String.format(url_prefix + "claim", SUPPORT.getLocalPort(), deviceId))
                .request().post(Entity.json(new DeviceClaim(person_id)));
        assertEquals(200, response.getStatus());
        response.close();

        List<DeviceContact> contacts = new ArrayList<>();
        for (int i=1; i<=10; i++) {
            contacts.add(new DeviceContact(getDeviceId(100+i),Instant.now().minusSeconds(i),i));
        }

        response = client.target(
                String.format(url_prefix + "contacts", SUPPORT.getLocalPort(), deviceId))
                .request().post(Entity.json(contacts));
        assertEquals(200, response.getStatus());
        response.close();


    }


}
