package com.viettel.vtag.service.impl;

import com.viettel.vtag.service.interfaces.MqttPublisher;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttPublisherImpl implements MqttPublisher {

    private final MqttClient client;

    public MqttPublisherImpl(@Qualifier("mqtt-publisher-client") MqttClient client) {
        this.client = client;
    }

    @Override
    public void publish(String topic, byte[] payload) {
        try {
            client.publish(topic, new MqttMessage(payload));
        } catch (MqttException e) {
            log.error("Couldn't pub {} bytes to {}", payload.length, topic);
        }
    }

    @Override
    public void publish(String topic, MqttMessage message) {
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            log.error("Couldn't pub {} bytes to {}", message.getPayload().length, topic);
        }
    }
}
