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
    private volatile Mqtt mqtt;

    private boolean mqttInitialized() {
        boolean mqttInitialized = mqtt != null;

        if (!mqttInitialized) {
            synchronized (this) {
                if (mqtt == null) {
                    var mqttConn = Config.instance().to().getString(Config.Entry.MQTT_ADDRESS.key(),
                            "tcp://localhost:1883");
                    var clientId = Config.instance().to().getString(Config.Entry.MQTT_CLIENT_ID.key(),
                            "PMStationClient");
                    var topic = Config.instance().to().getString(Config.Entry.MQTT_TOPIC.key(),
                            "pm-home-station");
                    var username = Config.instance().to().getString(Config.Entry.MQTT_USERNAME.key());
                    var password = Config.instance().to().getString(Config.Entry.MQTT_PASSWORD.key());
                    var reconnDelay = Config.instance().to().getInt(Config.Entry.MQTT_RECONNECT_DELAY.key(), 5);

                    mqtt = new Mqtt(mqttConn, clientId, topic, username, password, reconnDelay);
                    mqttInitialized = mqtt.connect();
                }
            }
        }

        return mqttInitialized;
    }

    @Override
    public void update(ParticulateMatterSample sample) {
        if (Config.instance().to().getBoolean(Config.Entry.MQTT_ENABLED.key(), false)) {
            logger.debug("MQTT going to send: {}", sample);

            if (mqttInitialized()) {
                mqtt.publish(sample);
            }
        }
    }

    @Override
    public void disconnecting() {
        if (Config.instance().to().getBoolean(Config.Entry.MQTT_ENABLED.key(), false) && mqtt != null) {
            mqtt.disconnect();
            mqtt = null;
            logger.info("MQTT observer shut down...");
        }
    }

    @Override
    public void disconnected() {
        logger.info("Sensor disconnected");
    }
}
