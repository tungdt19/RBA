package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.config.MqttSubscriberConfig;
import com.viettel.vtag.model.transfer.*;
import com.viettel.vtag.service.interfaces.DeviceMessageService;
import com.viettel.vtag.service.interfaces.FirebaseService;
import com.viettel.vtag.service.interfaces.GeoConvertService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.UUID;

/** @see MqttSubscriberConfig#mqttSubscriberClient(MqttCallback) */
@Slf4j
@Service
// @RequiredArgsConstructor
public class MqttHandler implements MqttCallback {

    private final ObjectMapper mapper = new ObjectMapper();

    private final MqttClient publisher;
    private final DeviceMessageService deviceService;
    private final FirebaseService firebaseService;
    private final GeoConvertService geoConvertService;

    public MqttHandler(
        @Qualifier("mqtt-publisher-client") MqttClient publisher,
        DeviceMessageService deviceService,
        FirebaseService firebaseService,
        GeoConvertService geoConvertService
    ) {
        this.publisher = publisher;
        this.deviceService = deviceService;
        this.firebaseService = firebaseService;
        this.geoConvertService = geoConvertService;
    }

    /** @see MqttCallback#connectionLost(Throwable) */
    @Override
    public void connectionLost(Throwable e) {
        log.error("MQTT connection lost", e);
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        var index = topic.indexOf('/', 9);
        var deviceId = UUID.fromString(topic.substring(9, index));
        var subtopic = topic.substring(index + 1);
        var payload = new String(message.getPayload());

        try {
            switch (subtopic) {
                case "data":
                    handleGpsMessage(deviceId, payload);
                    break;
                case "userdefined/wificell":
                    handleWifiCellMessage(deviceId, payload);
                    break;
                case "userdefined/battery":
                    updateBattery(deviceId, payload);
                    break;
                case "userdefined/devconf":
                    updateConfig(deviceId, payload);
                    break;
                default:
                    log.warn("Do not recognize subtopic '{}'", subtopic);
            }
        } catch (JsonProcessingException e) {
            log.error("Couldn't parse MQTT payload from sub topic '{}': {}", subtopic, e.getMessage());
        }
    }

    private void handleGpsMessage(UUID deviceId, String payload) throws JsonProcessingException {
        var gps = mapper.readValue(payload, LocationMessage.class);
        if ("DPOS".equals(gps.type())) {
            log.info("Received an GPS message: {}", payload);
            deviceService.saveLocation(deviceId, gps);
        }
    }

    private void handleWifiCellMessage(UUID deviceId, String payload) throws JsonProcessingException {
        var data = mapper.readValue(payload, CellWifiMessage.class);

        switch (data.type()) {
            case "DSOS":
                convertWifiCell(deviceId, data).subscribe(
                    locationMessage -> firebaseService.sos(deviceId, locationMessage));
                break;
            case "DWFC":
                convertWifiCell(deviceId, data).subscribe(locationMessage -> log.info("converted {}", locationMessage));
                break;
            default:
                log.info("Do not recognize message {}", payload);
        }
    }

    private void updateBattery(UUID deviceId, String payload) throws JsonProcessingException {
        var data = mapper.readValue(payload, BatteryMessage.class);
        deviceService.updateBattery(deviceId, data)
            .filter(updated -> updated > 0)
            .subscribe(updated -> log.info("Updated battery info ({}) for {}", data.level(), deviceId));
    }

    private void updateConfig(UUID deviceId, String payload) {
        try {
            var data = mapper.readValue(payload, ConfigMessage.class);
            deviceService.updateConfig(deviceId, data)
                .filter(updated -> updated > 0)
                .subscribe(updated -> log.info("Updated config info ({}) for {}", data.MMC().modeString(), deviceId));
        } catch (JsonProcessingException e) {
            log.error("Couldn't parse MQTT config payload: {}", e.getMessage());
        }
    }

    private Mono<LocationMessage> convertWifiCell(UUID deviceId, CellWifiMessage payload) {
        return geoConvertService.convert(deviceId, payload)
            .doOnNext(location -> log.info("{}: {}", deviceId, location))
            .doOnNext(location -> deviceService.saveLocation(deviceId, location))
            .map(location -> LocationMessage.fromLocation(location, payload))
            .doOnNext(location -> publishLocation(deviceId, location))
            .doOnError(e -> log.error("Error converting: {}", e.getMessage()));
    }

    private void publishLocation(UUID deviceId, LocationMessage location) {
        var topic = "messages/" + deviceId + "/data";
        try {
            var bytes = new MqttMessage();
            bytes.setPayload(mapper.writeValueAsBytes(location));
            publisher.publish(topic, bytes);
        } catch (MqttException | JsonProcessingException e) {
            log.error("Couldn't publish location {} to topic '{}'", location, topic, e);
        }
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            log.info("deliveryComplete {}: {}", Arrays.toString(token.getTopics()), token.getMessage());
        } catch (MqttException e) {
            log.error("Error on deliveryComplete", e);
        }
    }
}

