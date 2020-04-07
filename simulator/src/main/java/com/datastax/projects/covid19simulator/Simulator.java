package com.datastax.projects.covid19simulator;

import com.google.common.base.Preconditions;

import java.util.Random;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Simulator implements Iterable<Simulator.Encounter> {

    private static final Random random = new Random(515);

    private final int numPeople;
    private final int maxHours;
    private final int maxDistance;
    private final double contactProbability;
    private final double longExposureProb;
    private final double recordingBaselineProb;

    private static final long hourInMillis = 60*60*1000;
    private static final double exponentialNoiseReductionExponent = 1.0/100;

    private static final int[] SHORT_CONTACT_TIME_MIN_MAX = {1, 60};
    private static final int[] LONG_CONTACT_TIME_MIN_MAX = {300, 3600};



    public Simulator(SimulatorOption.Config config) {
        Preconditions.checkNotNull(config);

        numPeople = config.get(SimulatorOption.PEOPLE);
        maxHours = config.get(SimulatorOption.DURATION);
        maxDistance = config.get(SimulatorOption.DISTANCE);
        contactProbability = config.get(SimulatorOption.CONTACT_PROB);
        longExposureProb = config.get(SimulatorOption.EXPOSURE);
        recordingBaselineProb = 1.0-(double)config.get(SimulatorOption.NOISE);

        Preconditions.checkArgument(numPeople>=2, "Simulation requires at least 2 people to work");
        Preconditions.checkArgument(maxHours>0, "The duration needs to be a positive number");
        Preconditions.checkArgument(maxDistance>0, "The maximum distance needs to be a positive number");
        Preconditions.checkArgument( contactProbability>0.0 && contactProbability<1.0, "The contact probability needs to be bigger than 0 and smaller than 1");
        Preconditions.checkArgument( recordingBaselineProb>=0.0 && recordingBaselineProb<=1.0, "The noise probability needs to be bigger than 0 and smaller than 1");
        Preconditions.checkArgument( longExposureProb>=0.0 && longExposureProb<=1.0, "The long exposure probability needs to be bigger than 0 and smaller than 1");
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    public Stream<Encounter> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    private class Iterator implements java.util.Iterator<Simulator.Encounter> {

        private final long baseTime = System.currentTimeMillis()-maxHours*hourInMillis;
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
            long timeMillis = baseTime + currentHour*hourInMillis + random.nextInt((int)hourInMillis);
            return timeMillis;
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
            int duration = random.nextDouble()<longExposureProb?
                                generateInInterval(LONG_CONTACT_TIME_MIN_MAX):
                                generateInInterval(SHORT_CONTACT_TIME_MIN_MAX);
            double recordingProb = recordingBaselineProb + (1-recordingBaselineProb) *
                                    (1-Math.exp(-exponentialNoiseReductionExponent*duration));

            Encounter encounter = new Encounter(currentPerson, otherPerson, generateTimestamp(), duration, recordingProb);
            findNextContactPerson();
            return encounter;
        }

    }

    private static final int generateInInterval(int[] minmax) {
        Preconditions.checkArgument(minmax.length==2 && minmax[0]<minmax[1] && minmax[0]>=0);
        int delta = minmax[1]-minmax[0];
        return random.nextInt(delta)+minmax[0];
    }


    public static class Encounter {

        public final int person1;
        public final int person2;
        public final long timestamp;
        public final int duration;
        public final double recordingProb;

        public Encounter(int person1, int person2, long timestamp, int duration, double recordingProb) {
            this.person1 = person1;
            this.person2 = person2;
            this.timestamp = timestamp;
            this.duration = duration;
            this.recordingProb = recordingProb;
        }


        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append(person1).append("---").append(timestamp).append("|").append(duration).append("-->").append(person2);
            return s.toString();
        }
    }

}
