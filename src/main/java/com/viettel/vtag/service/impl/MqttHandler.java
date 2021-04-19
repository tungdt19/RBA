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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
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
    private final MessageSource messageSource;

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
        var payload = message.getPayload();

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
        } catch (IOException e) {
            log.error("{}> Couldn't parse MQTT payload from sub topic '{}': {}", deviceId, subtopic, e.getMessage());
        }
    }

    private void handleGpsMessage(UUID deviceId, byte[] payload) throws IOException {
        var gps = mapper.readValue(payload, LocationMessage.class);
        if (MSG_POSITION.equals(gps.type())) {
            deviceService.updateLocation(deviceId, gps)
                .subscribe(updated -> log.info("{}> {} bytes -> GPS ({}, {}) at {}: {}", deviceId, payload.length,
                    gps.latitude(), gps.longitude(), gps.timestamp(), updated));
        }
    }

    private void handleWifiCellMessage(UUID deviceId, byte[] payload) throws IOException {
        var data = mapper.readValue(payload, WifiCellMessage.class);
        var type = data.type();
        var device = deviceRepository.get(deviceId);
        switch (type) {
            case MSG_SOS:
                convertWifiCell(deviceId, data, payload).doOnNext(device::location)
                    .subscribe(location -> firebaseService.sos(device, location));
                break;
            case MSG_WIFI_CELL:
                convertWifiCell(deviceId, data, payload).doOnNext(device::location)
                    .map(location -> geoService.checkFencing(device, location))
                    .filter(FenceCheck::change)
                    .subscribe(fence -> firebaseService.notifySafeZone(device, fence));
                // fenceCheck.subscribe(fence -> {
                //     var message = messageSource.getMessage(fence.message(), fence.args(), Locale.ENGLISH);
                //     publisher.publish("messages/" + deviceId + "/data", message.getBytes(StandardCharsets.UTF_8));
                // });
                break;
            case MSG_TIME_REQUEST:
                log.info("{}> {} bytes -> DTIME", deviceId, payload.length);
                publisher.publish("messages/" + deviceId + "/app/controls", TimeMessage.toBytes());
                break;
            default:
                log.info("{}> {} bytes -> Don't handle '{}'", deviceId, payload.length, type);
        }
    }

    private void updateBattery(UUID deviceId, byte[] payload) throws IOException {
        var data = mapper.readValue(payload, BatteryMessage.class);
        deviceService.updateBattery(deviceId, data)
            .filter(updated -> updated > 0)
            .subscribe(updated -> log.info("{}> BTR {}%", deviceId, data.level()));
    }

    private void updateConfig(UUID deviceId, byte[] payload) throws IOException {
        var data = mapper.readValue(payload, ConfigMessage.class);
        if (MSG_CONFIG_UPDATE.equals(data.type())) {
            deviceService.updateConfig(deviceId, data)
                .filter(updated -> updated > 0)
                .subscribe(updated -> log.info("{}> CFG mode {}", deviceId, ConfigMessage.mode(data)));
        } else {
            log.info("{}> ignore {}", deviceId, data.type());
        }
    }

    private Mono<LocationMessage> convertWifiCell(UUID deviceId, WifiCellMessage message, byte[] payload) {
        return geoService.convert(deviceId, message)
            .doOnNext(location -> deviceService.updateLocation(deviceId, location))
            .map(location -> LocationMessage.fromLocation(location, message))
            .doOnNext(location -> publishLocation(deviceId, location))
            .doOnNext(
                location -> log.info("{}> {} bytes -> LOC {} ({}, {}, {})", deviceId, payload.length, message.type(),
                    location.latitude(), location.longitude(), location.accuracy()))
            .doOnError(e -> log.error("{}> Error converting: {}", deviceId, e.getMessage()));
    }

    private void publishLocation(UUID deviceId, LocationMessage location) {
        var topic = "messages/" + deviceId + "/data";
        try {
            publisher.publish(topic, mapper.writeValueAsBytes(location));
        } catch (JsonProcessingException e) {
            log.error("{}< Couldn't publish location {} to topic '{}'", deviceId, location, topic, e);
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
