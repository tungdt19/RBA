package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.config.MqttSubscriberConfig;
import com.viettel.vtag.model.entity.FenceCheck;
import com.viettel.vtag.model.transfer.*;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.service.interfaces.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.UUID;

/** @see MqttSubscriberConfig#mqttSubscriberClient(MqttCallback) */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqttHandler implements MqttCallback {

    private static final String MSG_SOS = "DSOS";
    private static final String MSG_POSITION = "DPOS";
    private static final String MSG_CONFIG_UPDATE = "DCF";
    private static final String MSG_TIME_REQUEST = "DTIME";
    private static final String MSG_WIFI_CELL = "DWFC";

    private final ObjectMapper mapper = new ObjectMapper();

    private final MqttPublisher publisher;
    private final DeviceMessageService deviceService;
    private final DeviceRepository deviceRepository;
    private final FirebaseService firebaseService;
    private final GeoService geoService;

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
        var p = message.getPayload();
        var payload = new String(p);
        log.info("{}> {} -> {} bytes", deviceId, subtopic, p.length);

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
                    log.warn("{}> Do not recognize subtopic '{}'", deviceId, subtopic);
            }
        } catch (JsonProcessingException e) {
            log.error("{}> Couldn't parse MQTT payload from sub topic '{}': {}", deviceId, subtopic, e.getMessage());
        }
    }

    private void handleGpsMessage(UUID deviceId, String payload) throws JsonProcessingException {
        var gps = mapper.readValue(payload, LocationMessage.class);
        if (MSG_POSITION.equals(gps.type())) {
            log.info("{}> GPS {}", deviceId, payload);
            deviceService.updateLocation(deviceId, gps).subscribe();
        }
    }

    private void handleWifiCellMessage(UUID deviceId, String payload) throws JsonProcessingException {
        var data = mapper.readValue(payload, WifiCellMessage.class);
        var type = data.type();
        log.info("{}> {}", deviceId, type);
        var device = deviceRepository.get(deviceId);
        switch (type) {
            case MSG_SOS:
                convertWifiCell(deviceId, data).subscribe(location -> firebaseService.sos(device, location));
                break;
            case MSG_WIFI_CELL:
                convertWifiCell(deviceId, data).map(location -> geoService.checkFencing(device, location))
                    .filter(FenceCheck::change)
                    .subscribe(fence -> firebaseService.notifySafeZone(device, fence));
                break;
            case MSG_TIME_REQUEST:
                log.info("{}> {}", deviceId, payload);
                publisher.publish("messages/" + deviceId + "/app/controls", TimeMessage.toBytes());
                break;
            default:
                log.info("{}> Do not recognize {}", deviceId, type);
        }
    }

    private void updateBattery(UUID deviceId, String payload) throws JsonProcessingException {
        var data = mapper.readValue(payload, BatteryMessage.class);
        deviceService.updateBattery(deviceId, data)
            .filter(updated -> updated > 0)
            .subscribe(updated -> log.info("{}> BTR {}", deviceId, data.level()));
    }

    private void updateConfig(UUID deviceId, String payload) {
        try {
            var data = mapper.readValue(payload, ConfigMessage.class);
            if (MSG_CONFIG_UPDATE.equals(data.type())) {
                deviceService.updateConfig(deviceId, data)
                    .filter(updated -> updated > 0)
                    .subscribe(updated -> log.info("{}> CFG {}", deviceId, ConfigMessage.mode(data)));
            } else {
                log.info("{}> ignore {}", deviceId, data.type());
            }
        } catch (JsonProcessingException e) {
            log.error("{}> Couldn't parse MQTT config payload: {}", deviceId, e.getMessage());
        }
    }

    private Mono<LocationMessage> convertWifiCell(UUID deviceId, WifiCellMessage payload) {
        return geoService.convert(deviceId, payload)
            .switchIfEmpty(geoService.retryConvert(deviceId, payload))
            .doOnNext(location -> log.info("{}> {} LOC ({}, {}, {})", deviceId, payload.type(), location.latitude(),
                location.longitude(), location.accuracy()))
            .doOnNext(location -> deviceService.updateLocation(deviceId, location))
            .map(location -> LocationMessage.fromLocation(location, payload))
            .doOnNext(location -> publishLocation(deviceId, location))
            .doOnError(e -> log.error("{}> Error converting: {}", deviceId, e.getMessage()));
    }

    private void publishLocation(UUID deviceId, LocationMessage location) {
        var topic = "messages/" + deviceId + "/data";
        try {
            publisher.publish(topic, mapper.writeValueAsBytes(location));
        } catch (JsonProcessingException e) {
            log.error("{}> Couldn't publish location {} to topic '{}'", deviceId, location, topic, e);
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
