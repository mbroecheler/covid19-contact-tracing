package com.datastax.projects.covid19simulator;

import com.google.common.base.Preconditions;

import java.util.Iterator;
import java.util.Random;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Simulator implements Iterable<Simulator.Encounter> {

    private static final Random random = new Random(515);

    private final int numPeople;
    private final int maxHours;
    private final int maxDistance;
    private final double contactProbability;

    private static final int hourInMillis = 60*60*1000;



    public Simulator(SimulatorOption.Config config) {
        Preconditions.checkNotNull(config);

        numPeople = config.get(SimulatorOption.PEOPLE);
        maxHours = config.get(SimulatorOption.DURATION);
        maxDistance = config.get(SimulatorOption.DISTANCE);
        contactProbability = config.get(SimulatorOption.CONTACT_PROB);

        Preconditions.checkArgument(numPeople>=2, "Simulation requires at least 2 people to work");
        Preconditions.checkArgument(maxHours>0, "The duration needs to be a positive number");
        Preconditions.checkArgument(maxDistance>0, "The maximum distance needs to be a positive number");
        Preconditions.checkArgument( contactProbability>0.0 && contactProbability<1.0, "The contact probability needs to be bigger than 0 and smaller than 1");
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    public Stream<Encounter> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    private class Iterator implements java.util.Iterator<Simulator.Encounter> {

        private final long baseTime = System.currentTimeMillis();
        private int currentHour = 0;
        private int currentPerson = -1;

        private Iterator() {
            findNextContactPerson();
        }

        private void findNextContactPerson() {
            while (currentHour < maxHours) {
                while (currentPerson < numPeople-1) {
                    currentPerson++;
                    if (random.nextDouble()<contactProbability) {
                        //This person is encountering somebody else
                        return;
                    } //else keep looking
                }
                currentPerson=-1;
                currentHour++;
            }
        }

        private long generateTimestamp() {
            return baseTime + currentHour*hourInMillis + random.nextInt(hourInMillis);
        }

        @Override
        public boolean hasNext() {
            return currentPerson>=0;
        }

        @Override
        public Encounter next() {
            //Determine whom currentPerson encounters
            int otherPerson = currentPerson;
            while (otherPerson==currentPerson) {
                int distance = (random.nextInt(maxDistance) + 1);
                if (random.nextBoolean()) distance = -distance;

                otherPerson = (numPeople+currentPerson+distance)%numPeople;
            }

            Encounter encounter = new Encounter(currentPerson, otherPerson, generateTimestamp());
            findNextContactPerson();
            return encounter;
        }

    }


    public static class Encounter {

        public final int person1;
        public final int person2;
        public final long timestamp;

        public Encounter(int person1, int person2, long timestamp) {
            this.person1 = person1;
            this.person2 = person2;
            this.timestamp = timestamp;
        }


        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append(person1).append("---").append(timestamp).append("-->").append(person2);
            return s.toString();
        }
    }

}
