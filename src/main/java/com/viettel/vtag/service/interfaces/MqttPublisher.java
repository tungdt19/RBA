package com.viettel.vtag.service.interfaces;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttPublisher {

    void publish(String topic, byte[] payload);

    void publish(String topic, MqttMessage message);

    void publish(String topic, byte[] payload, int qos, boolean retain);
}
