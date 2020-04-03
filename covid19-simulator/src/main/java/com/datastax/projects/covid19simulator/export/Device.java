package com.datastax.projects.covid19simulator.export;

import com.opencsv.bean.CsvBindByName;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class Device {

    @CsvBindByName(column = "device_id")
    private final String deviceId;

    @CsvBindByName(column = "last_sync")
    private final Instant lastSync;

    public Device(int prefix, int personId) {
        this.deviceId = PersonDevice.toDevideId(prefix, personId);
        this.lastSync = Instant.ofEpochMilli(System.currentTimeMillis());
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Instant getLastSync() {
        return lastSync;
    }
}
