package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.service.interfaces.MqttService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttServiceImpl implements MqttService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final MqttClient client;
    private final DeviceRepository deviceRepository;

    @Setter
    @Value("${vtag.mqtt.qos}")
    private int qos;

    @PostConstruct
    public void subscribeExistedDevices() {
        Mono.just(deviceRepository.fetchAllDevices()).subscribe(uuids -> {
            for (var id : uuids) {
                try {
                    client.subscribe(id.toString(), qos);
                } catch (MqttException e) {
                    log.error("sub", e);
                }
            }
        });
    }

    @Override
    public void subscribe(String topic, int qos) throws MqttException {
        var token = client.subscribeWithResponse(topic, qos);
        log.info("MQTT token {}: {}", token.isComplete(), Arrays.toString(token.getTopics()));
    }

    @Override
    public void publish(String topic, MqttMessage message) throws MqttException {
        log.info("Publishing to topic {}: {}", topic, message);
        client.publish(topic, message);
    }
}
