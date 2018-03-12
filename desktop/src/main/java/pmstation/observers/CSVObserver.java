/*
 * pm-home-station
 * 2017-2018 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.observers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.configuration.Config;
import pmstation.configuration.Constants;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;

public class CSVObserver implements IPlanTowerObserver {
    
    private static final Logger logger = LoggerFactory.getLogger(CSVObserver.class);
    
    private static final String LINE_FORMAT = "%s,%s,%s,%s,%s" + System.lineSeparator();

    @Override
    public void update(ParticulateMatterSample sample) {
        if (sample == null) {
            writeInfo(String.format(LINE_FORMAT, Constants.DATE_FORMAT.format(Calendar.getInstance().getTime()), "", "", "", "Device unavailable"));
        } else {
            writeInfo(String.format(LINE_FORMAT, Constants.DATE_FORMAT.format(sample.getDate()), sample.getPm1_0(), sample.getPm2_5(), sample.getPm10(), ""));
        }
    }
    
    @Override
    public void connecting() {
        writeInfo(String.format(LINE_FORMAT, Constants.DATE_FORMAT.format(Calendar.getInstance().getTime()), "", "", "", "Connecting to sensor..."));
    }
    
    @Override
    public void connected() {
        writeInfo(String.format(LINE_FORMAT, Constants.DATE_FORMAT.format(Calendar.getInstance().getTime()), "", "", "", "Sensor connected"));
    }
    
    @Override
    public void disconnected() {
        writeInfo(String.format(LINE_FORMAT, Constants.DATE_FORMAT.format(Calendar.getInstance().getTime()), "", "", "", "Sensor disconnected"));
    }
    
    @Override
    public void disconnecting() {
        writeInfo(String.format(LINE_FORMAT, Constants.DATE_FORMAT.format(Calendar.getInstance().getTime()), "", "", "", "Disconnecting from the sensor..."));
    }
    
    private void writeInfo(String info) {
        if (Config.instance().to().getBoolean(Config.Entry.CSV_LOG_ENABLED.key(), false)) {
            String csvFilePath = Config.instance().to().getString(Config.Entry.CSV_LOG_FILE.key());
            File csv = csvFilePath != null ? new File(csvFilePath) : null;
            if (csv == null || csv.exists() && (csv.isDirectory() || !csv.canWrite())) {
                logger.warn("Unable to write to: {} - {}", csvFilePath, csv);
                return;
            }
            
            try (FileWriter fw = new FileWriter(csv, true)) {
                fw.write(info);
            } catch (IOException ex) {
               logger.warn("Error writing to CSV file", ex);
            }
        }
    }
}
