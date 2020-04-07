package com.datastax.projects.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Contact {

    private UUID contact_id;

    private String first_contact;

    private int exposure_sec;

    public Contact() {
        // Jackson deserialization
    }

    public Contact(UUID contact_id, String first_contact, int exposure_sec) {
        this.contact_id = contact_id;
        this.first_contact = first_contact;
        this.exposure_sec = exposure_sec;
    }

    @JsonProperty
    public UUID getContact_id() {
        return contact_id;
    }

    @JsonProperty
    public String getFirst_contact() {
        return first_contact;
    }

    @JsonProperty
    public int getExposure_sec() {
        return exposure_sec;
    }

}
