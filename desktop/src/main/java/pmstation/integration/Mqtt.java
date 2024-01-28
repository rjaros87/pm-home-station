/*
 * pm-home-station
 * 2017-2024 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.integration;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmstation.configuration.Config;
import pmstation.core.plantower.ParticulateMatterSample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Mqtt {
    private static final Logger logger = LoggerFactory.getLogger(Mqtt.class);
    private static final Gson gson = new Gson();
    private final String topic;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private MqttClient client;

    public Mqtt() {
        topic = Config.instance().to().getString(Config.Entry.MQTT_TOPIC.key(),
                "pm-home-station/aqi");
        String broker = Config.instance().to().getString(Config.Entry.MQTT_BROKER.key(),
                "tcp://localhost:1883");
        String clientId = Config.instance().to().getString(Config.Entry.MQTT_CLIENT_ID.key(),
                "PMStationClient");

        try {
            client = new MqttClient(broker, clientId);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.info("Connection lost!");
                    scheduleReconnection(0);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    logger.info("Message received. Topic: {}, Message: {}", topic, message.getPayload());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });
        } catch (MqttException e) {
            logger.error("Unable to create a MQTT Client", e);
        }

        connect();
    }

    private void scheduleReconnection() {
        scheduleReconnection(Config.instance().to().getInt(Config.Entry.MQTT_RECONNECT_DELAY.key(), 5));
    }

    private void scheduleReconnection(Integer delay) {
        boolean isScheduled = !scheduler.isShutdown() && !scheduler.isTerminated();

        if (isScheduled) {
            scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
            logger.info("Reconnection scheduled within: {} s", delay);
        }
    }

    private void connect() {
        String username = Config.instance().to().getString(Config.Entry.MQTT_USERNAME.key());
        String password = Config.instance().to().getString(Config.Entry.MQTT_PASSWORD.key());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        if (Config.instance().to().getBoolean(Config.Entry.MQTT_ENABLED.key(), false)) {
            try {
                logger.info("Going to connect to MQTT Server: {}", client.getServerURI());
                client.connect(options);
                client.subscribe(topic);
            } catch (MqttException e) {
                logger.error("Unable to connect/subscribe to MQTT Server {}", client.getServerURI(), e);
                scheduleReconnection();

            }
        }
    }

    public void publish(ParticulateMatterSample sample) {
        if (client != null) {
            if (client.isConnected()) {
                MqttMessage message = new MqttMessage();
                String jsonString = gson.toJson(sample.getMap());
                message.setPayload(jsonString.getBytes());
                try {
                    client.publish(topic, message);
                } catch (MqttException e) {
                    logger.error("Unable to publish message", e);
                }
            } else {
                logger.error("MQTT Client not connected yet to server: {}.", client.getServerURI());
            }

        }
    }

    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                logger.error("Unable to disconnect from MQTT server: {}", client.getServerURI() ,e);
            }
        }
    }
}
