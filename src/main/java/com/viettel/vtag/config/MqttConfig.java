package com.viettel.vtag.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Slf4j
@Configuration
public class MqttConfig {

    @Value("${vtag.mqtt.url}")
    private String url;

    @Value("${vtag.mqtt.client-id}")
    private String clientId;

    @Value("${vtag.mqtt.username}")
    private String username;

    @Value("${vtag.mqtt.password}")
    private String password;

    // @Value("${vtag.mqtt.topic}")
    // private String topic;

    // @Value("${vtag.mqtt.qos}")
    // private int qos;

    @Value("${vtag.mqtt.timeout}")
    private int timeout;

    @Bean
    public MqttClient mqttClient(MqttCallback handler) throws MqttException {
        var client = new MqttClient(url, clientId, new MemoryPersistence());
        client.connect(connectOptions());
        client.setCallback(handler);
        return client;
    }

    public MqttConnectOptions connectOptions() {
        var options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(timeout);
        return options;
    }
}
