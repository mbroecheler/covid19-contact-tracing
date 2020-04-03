package com.datastax.projects;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.projects.db.DatabaseConfiguration;
import com.datastax.projects.db.DbSessionClientManager;
import com.datastax.projects.health.DatabaseHealthCheck;
import com.datastax.projects.resources.ContactTracingResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.net.InetSocketAddress;

public class ContactTracerApplication extends Application<ContactTracerConfiguration> {

    public static void main(final String[] args) throws Exception {
        new ContactTracerApplication().run(args);
    }

    @Override
    public String getName() {
        return "Contact-Tracer";
    }

    @Override
    public void initialize(final Bootstrap<ContactTracerConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final ContactTracerConfiguration configuration,
                    final Environment environment) {
        DbSessionClientManager sessionClientManager = new DbSessionClientManager(configuration.getDbConfig());

        environment.lifecycle().manage(sessionClientManager);

        final DatabaseHealthCheck dbHealthCheck = new DatabaseHealthCheck(sessionClientManager);
        environment.healthChecks().register("database", dbHealthCheck);


        final ContactTracingResource resource = new ContactTracingResource(
                sessionClientManager, configuration.getDefaultInfectionWindowHours()
        );
        environment.jersey().register(resource);
    }




}
