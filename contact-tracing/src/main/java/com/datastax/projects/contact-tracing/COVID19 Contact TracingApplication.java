package com.datastax.projects.contact-tracing;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class COVID19 Contact TracingApplication extends Application<COVID19 Contact TracingConfiguration> {

    public static void main(final String[] args) throws Exception {
        new COVID19 Contact TracingApplication().run(args);
    }

    @Override
    public String getName() {
        return "COVID19 Contact Tracing";
    }

    @Override
    public void initialize(final Bootstrap<COVID19 Contact TracingConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final COVID19 Contact TracingConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
