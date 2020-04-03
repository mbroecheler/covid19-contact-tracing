package com.datastax.projects;

import com.datastax.projects.db.DatabaseConfiguration;
import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ContactTracerConfiguration extends Configuration {
    @NotNull
    private int defaultInfectionWindowHours;

    @Valid
    @NotNull
    private DatabaseConfiguration dbConfig;

    @JsonProperty("database")
    public DatabaseConfiguration getDbConfig() {
        return dbConfig;
    }

    @JsonProperty("database")
    public void setDbConfig(DatabaseConfiguration dbConfig) {
        this.dbConfig = dbConfig;
    }

    @JsonProperty
    public int getDefaultInfectionWindowHours() {
        return defaultInfectionWindowHours;
    }

    @JsonProperty
    public void setDefaultInfectionWindowHours(int defaultInfectionWindowHours) {
        this.defaultInfectionWindowHours = defaultInfectionWindowHours;
    }
}
