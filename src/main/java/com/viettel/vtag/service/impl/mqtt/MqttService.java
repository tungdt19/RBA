package com.viettel.vtag.service.impl.mqtt;

import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor
public class MqttService implements MqttCallback {

    @Value("${vtag.mqtt.url}")
    private String url;

    @Value("${vtag.mqtt.client-id}")
    private String clientId;

    @Value("${vtag.mqtt.topic}")
    private String topic;

    @Value("${vtag.mqtt.username}")
    private String username;

    @Value("${vtag.mqtt.password}")
    private String password;

    @Value("${vtag.mqtt.qos}")
    private int qos;

    @Value("${vtag.mqtt.timeout}")
    private int timeout;

    private final MqttClient client;

    public void sendMessage(String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        this.client.publish(this.topic, message);
    }

    @Bean
    public MqttClient mqttClient(MqttConnectOptions connectOptions) throws MqttException {
        var client = new MqttClient(url, clientId, new MemoryPersistence());
        client.setCallback(this);
        client.connect(connectOptions);
        client.subscribe(this.topic, qos);
        return client;
    }

    @Bean
    public MqttConnectOptions connectOptions() {
        var options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(timeout);
        return options;
    }

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost because: " + cause);
        System.exit(1);
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        System.out.printf("[%s] %s%n", topic, new String(message.getPayload()));
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            System.out.println(token.getMessage());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

