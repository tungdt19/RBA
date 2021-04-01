package com.viettel.vtag;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class TestMqtt {

    private static final String url = "tcp://127.0.0.1";
    private static final String clientId = "test";
    private static final String topic = "test";
    private static final String username = "username";
    private static final String password = "password";
    private static final int qos = 2;
    private static final int timeout = 5000;

    public static void main(String[] args) throws MqttException {
        var client = mqttClient(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public static MqttClient mqttClient(MqttCallback callback) throws MqttException {
        var client = new MqttClient(url, clientId, new MemoryPersistence());
        client.connect(connectOptions());
        client.setCallback(callback);
        client.subscribe(topic, qos);
        return client;
    }

    public static MqttConnectOptions connectOptions() {
        var options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(timeout);
        return options;
    }
}
