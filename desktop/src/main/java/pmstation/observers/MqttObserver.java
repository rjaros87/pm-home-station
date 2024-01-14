/*
 * pm-home-station
 * 2017-2024 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.observers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmstation.core.plantower.IPlanTowerObserver;
import pmstation.core.plantower.ParticulateMatterSample;
import pmstation.integration.Mqtt;

public class MqttObserver implements IPlanTowerObserver {
    private static final Logger logger = LoggerFactory.getLogger(MqttObserver.class);
    private final Mqtt mqttClient;

    public MqttObserver() {
        mqttClient = Mqtt.getInstance();
    }

    @Override
    public void update(ParticulateMatterSample sample) {
        if (mqttClient != null) {
            logger.debug("MQTT going to send: {}", sample);
            mqttClient.publish(sample);
        }
    }

    @Override
    public void disconnecting() {
        mqttClient.disconnect();
        logger.info("Disconnecting from the sensor...");
    }

    @Override
    public void disconnected() {
        logger.info("Sensor disconnected");
    }
}
