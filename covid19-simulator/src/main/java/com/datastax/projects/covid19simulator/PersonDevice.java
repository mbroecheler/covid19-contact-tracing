package com.datastax.projects.covid19simulator;

import com.opencsv.bean.CsvBindByName;

import java.util.UUID;

public class PersonDevice {
    @CsvBindByName(column = "person uuid")
    private final UUID uuid;

    @CsvBindByName(column = "device type")
    private final int deviceType;

    @CsvBindByName(column = "device id")
    private final String deviceId;


    public PersonDevice(int personId) {
        this.uuid = UUID.randomUUID();
        this.deviceType = 1;
        this.deviceId = toDevideId(personId);
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public static String toDevideId(int personId) {
        return String.format("%09d", personId);
    }

}
