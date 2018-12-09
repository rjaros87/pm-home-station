/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */

package pmstation;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.configuration.Config;
import pmstation.configuration.Constants;
import pmstation.observers.CSVObserver;
import pmstation.observers.ConsoleObserver;
import pmstation.observers.UIScreenShotObserver;
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
            } else if (line.hasOption("version")) {
                System.out.println("version: " + Constants.VERSION);
            } else if (line.hasOption("headless")) {
                logger.info("Starting pm-home-station (headless mode), v.{} ({})...", Constants.VERSION, Constants.PROJECT_URL);
                PlanTowerSensor planTowerSensor = new PlanTowerSensor();
                planTowerSensor.addObserver(new ConsoleObserver());
                planTowerSensor.addObserver(new CSVObserver());
                planTowerSensor.connectDevice();
                if (planTowerSensor.isConnected()) {
                    planTowerSensor.startMeasurements(Config.instance().to().getInt(Config.Entry.INTERVAL.key(), Constants.DEFAULT_INTERVAL) * 1000L);
                    planTowerSensor.waitUntilDisconnected();
                    logger.info("Exiting!");
                    System.exit(0);
                } else {
                    logger.warn("Unable to connect to any sensor. Exiting!");
                    System.exit(1);
                }
            } else {
                logger.info("Starting pm-home-station, v.{} ({})...", Constants.VERSION, Constants.PROJECT_URL);
                setLookAndFeel();
                PlanTowerSensor planTowerSensor = new PlanTowerSensor();
                Station station = new Station(planTowerSensor);
                SwingUtilities.invokeLater(() -> { 
                    station.showUI();
                    if (line.hasOption("screenshot")) {
                        planTowerSensor.addObserver(new UIScreenShotObserver(station.getJFrame(), line.getOptionValue("screenshot")));
                    }
                });
            }
        } catch (ParseException e) {
            logger.error("Ooops", e);
            return;
        }
    }
    
    private static void setLookAndFeel() {
        if (SystemUtils.IS_OS_MAC_OSX) {
            // must be before any AWT interaction
            System.setProperty("apple.laf.useScreenMenuBar", "true"); // place menubar (if any) in native menu bar
            System.setProperty("apple.awt.application.name", Constants.PROJECT_NAME);
            if (Config.instance().to().getBoolean(Config.Entry.SYSTEM_TRAY.key(), Constants.SYSTEM_TRAY)) {
                logger.debug("Hiding dock icon since system-tray integration is enabled");
                System.setProperty("apple.awt.UIElement", "true");
            }
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            logger.error("Ooops, problem setting system L&F", e);
        }
    }
    
    private static Options getOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "print this message and exit");
        options.addOption("c", "headless", false, "headless mode (cli mode) - autostarts measurements based on config");
        options.addOption("s", "screenshot", true, "takes a screenshot of main window every measurment, the argument is filename where png will be (re)written");
        options.addOption("v", "version", false, "print version information and exit");
        return options;
    }
}
