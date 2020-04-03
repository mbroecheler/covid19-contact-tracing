#!/bin/zsh

loadFile ()
{
dsbulk load --schema.keyspace "$1" --schema.table "$2" -url "$3" -delim ',' --schema.allowMissingFields true
}

KEYSPACE=covid19
PREFIX=test_

loadFile $KEYSPACE person "${PREFIX}people.csv"
loadFile $KEYSPACE device "${PREFIX}device.csv"
loadFile $KEYSPACE person_owns_device "${PREFIX}people_device.csv"
loadFile $KEYSPACE device_contact "${PREFIX}device_contact.csv"
