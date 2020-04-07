package com.datastax.projects.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class DatabaseConfiguration {

    @NotEmpty
    @JsonProperty
    private String keyspace;

    @NotEmpty
    @JsonProperty
    private List<Seed> seeds;

    @JsonProperty
    private boolean createSchema;


    public String getKeyspace() {
        return keyspace;
    }

    public List<Seed> getSeeds() {
        return seeds;
    }

    public boolean isCreateSchema() {
        return createSchema;
    }

    public static class Seed {

        @NotEmpty
        @JsonProperty
        private String host;

        @NotNull
        @JsonProperty
        private int port;


        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
