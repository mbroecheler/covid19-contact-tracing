package com.datastax.projects.covid19simulator.export;

import com.opencsv.bean.CsvBindByName;

import java.util.UUID;

public class Person {

    @CsvBindByName(column = "person_id")
    private final UUID personId;

    public Person(int personId) {
        this.personId = PersonDevice.toUUID(personId);
    }

    public UUID getPersonId() {
        return personId;
    }
}
