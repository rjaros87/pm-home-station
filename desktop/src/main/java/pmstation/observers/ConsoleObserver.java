/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.observers;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pmstation.configuration.Constants;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;

public class ConsoleObserver implements IPlanTowerObserver {
    
    private static final Logger logger = LoggerFactory.getLogger(ConsoleObserver.class);

    @Override
    public void update(ParticulateMatterSample sample) {
        if (sample == null) {
            logger.warn(Instant.now().toString() + " sensor not ready");
        } else {
            logger.info("{} >>> PM1.0: {} [{}], PM2.5: {} [{}], PM10: {} [{}], "
                    + "HCHO: {} [{}], Humidity: {} [{}], Temperature: {} [{}], "
                    + "Model revision: {}, Error Code: {}",
                    Constants.DATE_FORMAT.format(sample.getDate()),
                    sample.getPm1_0(), Constants.PM_UNITS,
                    sample.getPm2_5(), Constants.PM_UNITS,
                    sample.getPm10(), Constants.PM_UNITS,
                    formatNonNeg("%.3f", ((double)sample.getHcho())/1000), Constants.HHCO_MG_UNITS,
                    formatNonNeg("%.1f", sample.getHumidity()), Constants.HUMI_UNITS,
                    formatNonNan("%.1f", sample.getTemperature()), Constants.TEMP_UNITS,
                    "0x" + String.format("%02X", sample.getModelVersion()),
                    "0x" + String.format("%02X", sample.getErrCode())
            );
        }
    }
    
    @Override
    public void disconnected() {
        logger.info("Sensor disconnected");
    }
    
    @Override
    public void disconnecting() {
        logger.info("Disconnecting from the sensor...");
    }
    
    private String formatNonNeg(String doubleFormat, double value) {
        return value >= 0 ? String.format(doubleFormat, value) : "-";
    }
    
    private String formatNonNan(String doubleFormat, double value) {
        return value != Double.NaN ? String.format(doubleFormat, value) : "-";
    }
}
