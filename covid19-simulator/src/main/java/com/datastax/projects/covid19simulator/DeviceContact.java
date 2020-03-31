package com.datastax.projects.covid19simulator;

import com.opencsv.bean.CsvBindByName;

import java.util.UUID;

public class DeviceContact {

    @CsvBindByName(column = "timestamp millis")
    private final long timestamp;

    @CsvBindByName(column = "device1 type")
    private final int device1Type;

    @CsvBindByName(column = "device1 id")
    private final String device1Id;

    @CsvBindByName(column = "device2 type")
    private final int device2Type;

    @CsvBindByName(column = "device2 id")
    private final String device2Id;

    public DeviceContact(Simulator.Encounter encounter) {
        device1Type = 1;
        device1Id = PersonDevice.toDevideId(encounter.person1);
        device2Type = 2;
        device2Id = PersonDevice.toDevideId(encounter.person2);
        timestamp = encounter.timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getDevice1Type() {
        return device1Type;
    }

    public String getDevice1Id() {
        return device1Id;
    }

    public int getDevice2Type() {
        return device2Type;
    }

    public String getDevice2Id() {
        return device2Id;
    }
}
