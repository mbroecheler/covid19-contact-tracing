package com.datastax.projects.covid19simulator;

import com.google.common.base.Preconditions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.HashMap;
import java.util.Map;

public enum SimulatorOption {

    PEOPLE(Option.builder("n").longOpt("people").desc("The number of people to simulate").hasArg().argName("num").build(),
                Integer.class, 10000),

    DURATION(Option.builder("l").longOpt("length").desc("The length of the simulation in hours").hasArg().argName("hours").build(),
    Integer.class, 24*14),

    DISTANCE(Option.builder("d").longOpt("distance").desc("The maximum distance people travel in the grid").hasArg().argName("cells").build(),
    Integer.class, 100),

    CONTACT_PROB(Option.builder("p").longOpt("probability").desc("The probability that a person meets another person in a given hour").hasArg().argName("prob").build(),
    Double.class, 0.1),

    NOISE(Option.builder("s").longOpt("noise").desc("The probability that short encounters are not properly recorded by a device").hasArg().argName("prob").build(),
            Double.class, 0.5),

    EXPOSURE(Option.builder("e").longOpt("exposure").desc("The probability that encounter has a long exposure (in contrast to short passings)").hasArg().argName("prob").build(),
            Double.class, 0.2);


    private final Option cmdOption;
    private final Class type;
    private final Object defaultValue;


    private<T> SimulatorOption(Option cmdOption, Class<T> type, T defaultValue) {
        Preconditions.checkNotNull(cmdOption);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(defaultValue);

        this.cmdOption = cmdOption;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return cmdOption.getOpt();
    }

    public static Options getOptions() {
        Options options = new Options();
        for (SimulatorOption opt : values()) {
            options.addOption(opt.cmdOption);
        }
        return options;
    }

    public static class Config {

        private final Map<SimulatorOption, Object> map = new HashMap<>(SimulatorOption.values().length);

        public Config() {

        }

        public void add(SimulatorOption option, Object value) {
            Preconditions.checkNotNull(option);
            Preconditions.checkNotNull(value);

            Preconditions.checkArgument(option.type.isInstance(value),"Invalid value [%] provided for option [%]", value, option);
            map.put(option,value);
        }

        public<T> T get(SimulatorOption option) {
            Object value = map.get(option);
            if (value==null) {
                value = option.defaultValue;
            }
            return (T)value;
        }

        public static Config getConfig(CommandLine cmdLine) {
            Config config = new Config();
            for (SimulatorOption o : values()) {
                if (cmdLine.hasOption(o.cmdOption.getOpt())) {
                    config.add(o, cmdLine.getOptionValue(o.cmdOption.getOpt()));
                }
            }
            return config;
        }


    }

}
