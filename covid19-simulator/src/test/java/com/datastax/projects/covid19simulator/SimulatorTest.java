package com.datastax.projects.covid19simulator;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class SimulatorTest
{

    @Test
    public void fullTest() {
        CmdLine.main(new String[]{"-f test_"});
    }

    @Test
    public void fileOutput() {
        CmdLine.run("test2_", getConfig(10000, 24*28, 50, 0.1));
    }

    @Test
    public void simpleTest() {
        Simulator simulator = new Simulator(getConfig(1000, 24, 5, 0.01));
        System.out.println("All encounters:");
        Iterator<Simulator.Encounter> sim = simulator.iterator();
        while (sim.hasNext()) {
            System.out.println(sim.next());
        }
    }

    @Test
    public void roughCount() {
        int[] numPeople = {10000, 50000, 100000};
        int[] numHours = {100, 500, 1000};
        double[] prob = {0.05, 0.1, 0.2, 0.4};

        for (int np : numPeople) {
            for (int nh : numHours) {
                for (double p : prob) {
                    roughCount(np, nh, p);
                }
            }
        }
    }

    @Test
    public void roughCountPerformance() {
        roughCount(1000000, 1000, 0.01);
    }

    public void roughCount(int numPeople, int numHours, double prob) {
        Simulator simulator = new Simulator(getConfig(numPeople, numHours, 4, prob));
        int count = 0;
        Iterator<Simulator.Encounter> sim = simulator.iterator();
        while (sim.hasNext()) {
            sim.next();
            count++;
        }
        double expected = numPeople*numHours*prob;
        double variation = Math.abs((count-expected)/expected);
//        System.out.println("Counted: " + count + " vs. expected: " + expected);
//        System.out.println("Deviation: " + variation);
        assertTrue(variation<0.01, count + " vs " + expected);
    }


    public static SimulatorOption.Config getConfig(int numPeople, int numHours, int maxDistance, double probability) {
        SimulatorOption.Config config = new SimulatorOption.Config();
        config.add(SimulatorOption.PEOPLE, numPeople);
        config.add(SimulatorOption.DURATION, numHours);
        config.add(SimulatorOption.DISTANCE, maxDistance);
        config.add(SimulatorOption.CONTACT_PROB, probability);
        return config;
    }
}
