package com.datastax.projects.covid19simulator.export;

import com.opencsv.bean.CsvBindByName;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;

public class PersonDevice {
    @CsvBindByName(column = "person_id")
    private final UUID personId;

    @CsvBindByName(column = "device_id")
    private final String deviceId;

    @CsvBindByName(column = "claimed_on")
    private final Instant claimedOn;


    public PersonDevice(int devicePrefix, int personId) {
        this.personId = toUUID(personId);
        this.deviceId = toDevideId(devicePrefix, personId);
        this.claimedOn = Instant.ofEpochMilli(System.currentTimeMillis());
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Instant getClaimedOn() {
        return claimedOn;
    }

    public static String toDevideId(int prefix, int personId) {
        return String.format("%02d", prefix) + "#" + String.format("%09d", personId);
    }

    public static UUID toUUID(int personId) {
        return UUID.nameUUIDFromBytes(ByteBuffer.allocate(4).putInt(personId).array());
    }

}
