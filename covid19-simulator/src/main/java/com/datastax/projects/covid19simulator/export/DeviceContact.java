package com.datastax.projects.covid19simulator.export;

import com.datastax.projects.covid19simulator.Simulator;
import com.opencsv.bean.CsvBindByName;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class DeviceContact {

    private static final Random random = new Random(342);
    private static final int RECORDING_OFFSET_MILLIS_MAX = 10000;

    @CsvBindByName(column = "device1_id")
    private final String device1Id;

    @CsvBindByName(column = "device2_id")
    private final String device2Id;

    @CsvBindByName(column = "time")
    private final Instant timestamp;

    @CsvBindByName(column = "duration_sec")
    private final int duration;

    public DeviceContact(String device1Id, String device2Id, Instant timestamp, int duration) {
        this.device1Id = device1Id;
        this.device2Id = device2Id;
        this.timestamp = timestamp;
        this.duration = duration;
    }


    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDevice1Id() {
        return device1Id;
    }

    public String getDevice2Id() {
        return device2Id;
    }

    public int getDuration() {
        return duration;
    }

    public static Stream<DeviceContact> getDeviceContacts(Simulator.Encounter encounter) {
        List<DeviceContact> contacts = new ArrayList<>(2);

        String device1Id = PersonDevice.toDevideId((int)encounter.timestamp & 1, encounter.person1);
        String device2Id = PersonDevice.toDevideId((int)encounter.timestamp>>2 & 1, encounter.person2);
        Instant timestamp = Instant.ofEpochMilli(encounter.timestamp);

        //Capture the encounter from the perspective of both devices subject to noise (i.e. recording probability)
        if (random.nextDouble()<encounter.recordingProb) {
            contacts.add(new DeviceContact(device1Id, device2Id, timestamp, encounter.duration));
        }
        if (random.nextDouble()<encounter.recordingProb) {
            //The other device will likely record a slightly different timestamp
            contacts.add(new DeviceContact(device2Id, device1Id, timestamp.minusMillis(random.nextInt(RECORDING_OFFSET_MILLIS_MAX)), encounter.duration));
        }

        return contacts.stream();
    }

}
