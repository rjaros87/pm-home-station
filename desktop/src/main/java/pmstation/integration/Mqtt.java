/*
 * pm-home-station
 * 2017-2024 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.integration;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import pmstation.configuration.Constants;
import pmstation.core.plantower.ParticulateMatterSample;

public class Mqtt {
    private static final String ONLINE = "online";
    private static final String OFFLINE = "offline";

    private static final Logger logger = LoggerFactory.getLogger(Mqtt.class);
    private static final Gson gson = new Gson();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private MqttClient client;
    private String lastError;

    private String topic;
    private String username;
    private String password;
    private int reconnectionDelay;

    private enum SubTopic {
        AQI,
        STATUS,
    }

    public Mqtt(String mqttConnection, String clientId, String topic, String username, String password, int reconnectionDelay) {
        this.topic = topic;
        this.username = username;
        this.password = password;
        this.reconnectionDelay = reconnectionDelay;

        try {
            client = new MqttClient(mqttConnection, clientId);
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
    }
    
    public void advertise(String advertisementTopic, String deviceId) {
        if (client == null || StringUtils.isEmpty(deviceId)) {
            return;
        }
        if (client.isConnected()) {
            for (SubTopic sub : SubTopic.values()) {
                Map<String, Object> adv = Map.of(
                        "name", sub.name().toLowerCase(),
                        "uniq_id", (deviceId + "-" + sub.name()).toLowerCase(),
                        "~", topic,
                        "device", Map.of(
                                // "ids", ? 
                                "mf", Constants.PROJECT_URL,
                                "name", Constants.PROJECT_NAME,
                                "sw", Constants.VERSION
                        )
                    );                    
                String jsonString = gson.toJson(adv);
                publishMessageToTopic(advertisementTopic,
                        Constants.PROJECT_NAME + "-" + deviceId + "/" + sub.name() + "/config", jsonString);
                logger.debug("MQTT advertisement sent: {}", adv);
            }
            lastError = null;
        } else {
            logger.error("MQTT Client not yet connected to server: {}", client.getServerURI());
        }
    }

    public void publish(ParticulateMatterSample sample) {
        if (client != null) {
            if (client.isConnected()) {
                String jsonString = gson.toJson(sample.getAsMap());
                publishMessageToTopic(SubTopic.AQI.name(), jsonString);

                publishMessageToTopic(SubTopic.STATUS.name(), ONLINE);
                lastError = null;
            } else {
                logger.error("MQTT Client not yet connected to server: {}", client.getServerURI());
            }

        }
    }

    public boolean connect() {
        if (client != null && client.isConnected()) {
            return true;
        }
        var connected = false;
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        try {
            logger.info("Going to connect to MQTT Server: {}", client.getServerURI());
            client.connect(options);
            client.subscribe(topic);
            lastError = null;
            connected = true;
        } catch (MqttException e) {
            lastError = ExceptionUtils.getRootCauseMessage(e);
            logger.error("Unable to connect/subscribe to MQTT Server {}", client.getServerURI(), e);
            scheduleReconnection(reconnectionDelay);
        }

        return connected;
    }

    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                publishMessageToTopic(SubTopic.STATUS.name(), OFFLINE);
                client.disconnect();
            } catch (MqttException e) {
                lastError = ExceptionUtils.getRootCauseMessage(e);
                logger.error("Unable to disconnect from MQTT server: {}", client.getServerURI() ,e);
            }
        }
    }

    public String getLastError() {
        return lastError;
    }

    private void scheduleReconnection(int delay) {
        if (reconnectionDelay > 0) {
            scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
            logger.info("Reconnection scheduled within: {}s", delay);
        }
    }
    
    private void publishMessageToTopic(String subtopic, String message) {
        publishMessageToTopic(topic, subtopic, message);
    }

    private void publishMessageToTopic(String topic, String subtopic, String message) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        try {
            client.publish(String.join("/", topic, subtopic.toLowerCase()), mqttMessage);
        } catch (MqttException e) {
            lastError = e.getMessage();
            logger.error("Unable to publish message", e);
        }
    }
}
