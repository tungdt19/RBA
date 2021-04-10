package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.service.interfaces.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Slf4j
@Service
public class MqttServiceImpl implements MqttService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final DeviceRepository deviceRepository;

    private final MqttClient subscriber;
    private final MqttClient publisher;
    private final int qos;

    public MqttServiceImpl(
        DeviceRepository deviceRepository,
        @Qualifier("mqtt-subscriber-client") MqttClient subscriber,
        @Qualifier("mqtt-publisher-client") MqttClient publisher,
        @Value("${vtag.mqtt.qos}") int qos
    ) {
        this.deviceRepository = deviceRepository;
        this.subscriber = subscriber;
        this.publisher = publisher;
        this.qos = qos;
    }

    @PostConstruct
    public void subscribeExistedDevices() {
        //@formatter:off
        var uuids = deviceRepository.fetchAllDevices();
        log.info("Subscribing to topic: {}", uuids);
        for (var uuid : uuids) {
            try {
                subscriber.subscribe(new String[] {
                    "messages/" + uuid + "/data",
                    "messages/" + uuid + "/userdefined/battery",
                    "messages/" + uuid + "/userdefined/wificell",
                    "messages/" + uuid + "/userdefined/devconf"});
            } catch (MqttException e) {
                log.error("Cannot subscribe to uuid {}: {}", uuid, e.getMessage());
            }
        }
        //@formatter:on
    }

    @Override
    public void subscribe(String[] topics) {
        try {
            subscriber.subscribe(topics, new int[] {qos});
        } catch (MqttException e) {
            log.error("Couldn't subscribe to topics {}", Arrays.toString(topics), e);
        }
    }

    @Override
    public void subscribe(String topic, int qos) throws MqttException {
        var token = subscriber.subscribeWithResponse(topic, qos);
        log.info("MQTT token {}: {}", token.isComplete(), Arrays.toString(token.getTopics()));
    }

    @Override
    public void publish(String topic, MqttMessage message) throws MqttException {
        log.info("Publishing to topic {}: {}", topic, message);
        publisher.publish(topic, message);
    }

    @Override
    public void publish(String topic, Object object) throws MqttException {
        try {
            var bytes = mapper.writeValueAsBytes(object);
            var message = new MqttMessage();
            message.setPayload(bytes);
            publisher.publish(topic, message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(String[] topics) {
        try {
            subscriber.unsubscribe(topics);
        } catch (MqttException e) {
            log.error("Couldn't subscribe to topics {}", Arrays.toString(topics), e);
        }
    }
}
