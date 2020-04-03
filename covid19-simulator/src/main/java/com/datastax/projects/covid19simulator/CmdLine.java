package com.datastax.projects.covid19simulator;

import com.datastax.projects.covid19simulator.csv.AnnotationStrategy;
import com.datastax.projects.covid19simulator.export.Device;
import com.datastax.projects.covid19simulator.export.DeviceContact;
import com.datastax.projects.covid19simulator.export.Person;
import com.datastax.projects.covid19simulator.export.PersonDevice;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CmdLine {

    public static final String PEOPLE_FILENAME = "people.csv";
    public static final String DEVICE_FILENAME = "device.csv";
    public static final String PEOPLE_TO_DEVICE_FILENAME = "people_device.csv";
    public static final String DEVICE_CONTACT_FILENAME = "device_contact.csv";

    public static final String FILE_OPTION_NAME = "fileprefix";


    public static void main( String[] args ) {
        CommandLine line = parseArguments(args);

        if (line.hasOption(FILE_OPTION_NAME)) {
            String fileNamePrefix = line.getOptionValue(FILE_OPTION_NAME).trim();
            SimulatorOption.Config config = SimulatorOption.Config.getConfig(line);
            run(fileNamePrefix, config);
        } else {
            printAppHelp();
        }
    }

    public static void run(String filenamePrefix, SimulatorOption.Config config) {
        int numPeople = config.get(SimulatorOption.PEOPLE);

        //Write out all people
        writeStream2CSV(IntStream.iterate(0, i -> i + 1).limit(numPeople).mapToObj(i -> new Person(i)),
                Person.class,filenamePrefix + PEOPLE_FILENAME);

        //Write out all devices
        writeStream2CSV(
                Stream.concat(
                    IntStream.iterate(0, i -> i + 1).limit(numPeople).mapToObj(i -> new Device(0, i)),
                    IntStream.iterate(0, i -> i + 1).limit(numPeople).mapToObj(i -> new Device(1, i))
                 ),
                 Device.class,filenamePrefix + DEVICE_FILENAME);

        //Write out the people->device mapping
        writeStream2CSV(
                Stream.concat(
                    IntStream.iterate(0, i -> i + 1).limit(numPeople).mapToObj(i -> new PersonDevice(0,i)),
                    IntStream.iterate(0, i -> i + 1).limit(numPeople).mapToObj(i -> new PersonDevice(1,i))
                ),
                PersonDevice.class,filenamePrefix + PEOPLE_TO_DEVICE_FILENAME);

        //Write out the device contacts mapping
        Simulator sim = new Simulator(config);
        writeStream2CSV(sim.stream().flatMap(e -> DeviceContact.getDeviceContacts(e)),
                DeviceContact.class, filenamePrefix + DEVICE_CONTACT_FILENAME);
    }

    public static<T> void writeStream2CSV(Stream<T> beanStream, Class<T> clazz, String filename) {
        File file = new File(filename);
        try (FileWriter writer = new FileWriter(file)) {
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).withMappingStrategy(new AnnotationStrategy(clazz)).build();
            beanToCsv.write(beanStream);
        } catch (IOException e) {
            System.err.println("Could not write csv file: " + e.getMessage());
        } catch (CsvException e) {
            System.err.println("Error converting to CSV: " + e.getMessage());
        }
    }

    /**
     * Parses application arguments
     *
     * @param args application arguments
     * @return <code>CommandLine</code> which represents a list of application
     * arguments.
     */
    private static CommandLine parseArguments(String[] args) {

        CommandLine line = null;
        CommandLineParser parser = new DefaultParser();

        try {
            line = parser.parse(getOptions(), args);

        } catch (ParseException ex) {

            System.err.println("Failed to parse command line arguments");
            System.err.println(ex.toString());
            printAppHelp();

            System.exit(1);
        }

        return line;
    }

    /**
     * Generates application command line options
     *
     * @return application <code>Options</code>
     */
    private static Options getOptions() {
        Options options = SimulatorOption.getOptions();
        options.addOption("f", FILE_OPTION_NAME, true, "Prefix of the csv filenames to write to");
        return options;
    }

    /**
     * Prints application help
     */
    private static void printAppHelp() {
        Options options = getOptions();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("COVID19 Contact Data Simulator", options, true);
    }

}
