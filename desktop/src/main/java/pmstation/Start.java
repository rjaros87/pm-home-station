/*
 * pm-station-usb
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-station-usb
 * License: GPL 3.0
 */

package pmstation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.configuration.Constants;
import pmstation.plantower.PlanTowerSensor;

public class Start {
    
    private static final Logger logger = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            Options options = getOptions();
            CommandLine line = parser.parse(options, args );
            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                System.out.println(Constants.PROJECT_NAME + ", " + Constants.PROJECT_URL);
                formatter.printHelp(Constants.PROJECT_NAME, options, true);
                return;
            } else if (line.hasOption("version")) {
                System.out.println("version: " + Constants.VERSION);
                return;
            }
        } catch (ParseException e) {
            logger.error("Ooops", e);
            return;
        }
        
        logger.info("Starting pm-station-usb ({})...", Constants.PROJECT_URL);
        PlanTowerSensor planTowerSensor = new PlanTowerSensor();
        Station station = new Station(planTowerSensor);
        station.showUI();
    }
    
    private static Options getOptions() {
        Options options = new Options();
        //options.addOption("noui", false, "no UI, console only");
        options.addOption("h", "help", false, "print this message and exit");
        options.addOption("v", "version", false, "print the version information and exit");
        return options;
    }
}
