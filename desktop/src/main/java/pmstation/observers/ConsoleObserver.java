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
            logger.info("{} >>> PM1.0: {}, PM2.5: {}, PM10: {}", Constants.DATE_FORMAT.format(sample.getDate()), sample.getPm1_0(), sample.getPm2_5(), sample.getPm10());
        }
    }
    
    @Override
    public void disconnected() {
        logger.info("Sensor disconnected");
    }
}
