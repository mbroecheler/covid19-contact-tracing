package com.datastax.projects.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class DeviceContact {

    @NotEmpty
    private String other_id;

    @NotNull
    private Instant timestamp;

    @Min(value=1)
    private int duration;

    public DeviceContact() {

    }

    public DeviceContact(@NotEmpty String other_id, @NotNull Instant timestamp, @Min(value = 1) int duration) {
        this.other_id = other_id;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    @JsonProperty
    public String getOther_id() {
        return other_id;
    }

    @JsonProperty
    public void setOther_id(String other_id) {
        this.other_id = other_id;
    }

    @JsonProperty
    public Instant getTimestamp() {
        return timestamp;
    }

    @JsonProperty
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty
    public int getDuration() {
        return duration;
    }

    @JsonProperty
    public void setDuration(int duration) {
        this.duration = duration;
    }
}
