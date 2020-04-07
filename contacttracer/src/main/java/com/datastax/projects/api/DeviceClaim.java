package com.datastax.projects.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class DeviceClaim {

    @NotNull
    private UUID person_id;

    public DeviceClaim() {
        // Jackson deserialization
    }

    public DeviceClaim(@NotNull UUID person_id) {
        this.person_id = person_id;
    }

    @JsonProperty
    public UUID getPerson_id() {
        return person_id;
    }

    @JsonProperty
    public void setPerson_id(UUID person_id) {
        this.person_id = person_id;
    }
}
