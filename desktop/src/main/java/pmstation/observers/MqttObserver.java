/*
 * pm-home-station
 * 2017-2024 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.observers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmstation.configuration.Config;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.integration.Mqtt;

public class MqttObserver implements IPlanTowerObserver {
    private static final Logger logger = LoggerFactory.getLogger(MqttObserver.class);

    @Override
    public void update(ParticulateMatterSample sample) {
        if (Config.instance().to().getBoolean(Config.Entry.MQTT_ENABLED.key(), false)) {
            logger.debug("MQTT going to send: {}", sample);
            Mqtt.getInstance().publish(sample);
        }
    }

    @Override
    public void disconnecting() {
        if (Config.instance().to().getBoolean(Config.Entry.MQTT_ENABLED.key(), false)) {
            Mqtt.getInstance().disconnect();
            logger.info("MQTT observer shut down...");
        }
    }

    @Override
    public void disconnected() {
        logger.info("Sensor disconnected");
    }
}
