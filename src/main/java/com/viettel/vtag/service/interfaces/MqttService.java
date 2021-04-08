package com.viettel.vtag.service.interfaces;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttService {

    void subscribe(String[] topics);

    void subscribe(String topic, int qos) throws MqttException;

    void unsubscribe(String[] topics);

    void publish(String topic, MqttMessage message) throws MqttException;
}
