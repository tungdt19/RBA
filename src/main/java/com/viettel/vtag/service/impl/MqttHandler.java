package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.config.MqttConfig;
import com.viettel.vtag.model.entity.Location;
import com.viettel.vtag.model.transfer.BatteryMessage;
import com.viettel.vtag.model.transfer.ConfigMessage;
import com.viettel.vtag.model.transfer.LocationMessage;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.repository.interfaces.LocationHistoryRepository;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/** @see MqttConfig#mqttClient(MqttCallback) */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqttHandler implements MqttCallback {

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient;
    private final DeviceRepository deviceRepository;
    private final FirebaseService firebaseService;
    private final UserRepository userRepository;
    private final LocationHistoryRepository locationHistory;

    @Value("${vtag.unwired.base-url}")
    private String convertAddress;

    @Value("${vtag.unwired.uri}")
    private String convertUri;

    @Value("${vtag.unwired.token}")
    private String convertToken;

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    @Override
    public void connectionLost(Throwable e) {
        log.error("MQTT connection: {}", e.getMessage());
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
        log.info("Receive from {}: {}", topic, payload);

        switch (subtopic) {
            case "wificell":
            case "data":
                convertLocation(subtopic, deviceId, payload);
                break;
            case "battery":
                updateBattery(subtopic, deviceId, payload);
                break;
            case "devconf":
                updateConfig(subtopic, deviceId, payload);
                break;
        }
    }

    private void convertLocation(String subtopic, UUID deviceId, String payload) {
        try {
            var data = mapper.readValue(payload, LocationMessage.class);

            switch (data.type()) {
                case "DSOS":
                    handleSosMessage(subtopic, deviceId, data);
                    break;
                case "DPOS":
                    handleGpsMessage(subtopic, deviceId, data);
                    break;
                case "DWFC":
                    handleWifiMessage(subtopic, deviceId, data);
                    break;
                default:
                    log.info("Do not recognize message {}", payload);
            }
        } catch (JsonProcessingException e) {
            log.error("Couldn't parse MQTT payload: {}", e.getMessage());
        }
    }

    private void updateBattery(String subtopic, UUID deviceId, String payload) {
        try {
            var data = mapper.readValue(payload, BatteryMessage.class);
            var updated = deviceRepository.updateBattery(deviceId, data);
            if (updated > 0) {
                log.info("Updated battery info ({}) for {}", data.level(), deviceId);
            }
        } catch (JsonProcessingException e) {
            log.error("Couldn't parse MQTT battery payload: {}", e.getMessage());
        }
    }

    private void updateConfig(String subtopic, UUID deviceId, String payload) {
        try {
            var data = mapper.readValue(payload, ConfigMessage.class);
            var updated = deviceRepository.updateConfig(deviceId, data);
            if (updated > 0) {
                log.info("Updated config info ({}) for {}", data.MMC().modeString(), deviceId);
            }
        } catch (JsonProcessingException e) {
            log.error("Couldn't parse MQTT config payload: {}", e.getMessage());
        }
    }

    private void handleSosMessage(String subtopic, UUID deviceId, LocationMessage payload) {
        log.info("Received an SOS message: {}", payload);
        var locationMono = convert(payload).doOnNext(location -> log.info("Converted: {}", location));

        locationMono.subscribe(location -> notifyApp(deviceId, location));
        locationMono.subscribe(location -> locationHistory.save(deviceId, location));
    }

    private void handleGpsMessage(String subtopic, UUID deviceId, LocationMessage payload) {
        log.info("Received an GPS message: {}", payload);

        locationHistory.save(deviceId, payload);
    }

    private void handleWifiMessage(String subtopic, UUID deviceId, LocationMessage payload) {
        log.info("Received an Wifi message: {}", payload);
        convert(payload).subscribe(location -> locationHistory.save(deviceId, location));
    }

    public Mono<Location> convert(LocationMessage json) {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(convertAddress)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(convertUri)
            .bodyValue(json.token(convertToken))
            .exchange()
            .filter(response -> response.statusCode().is2xxSuccessful())
            .flatMap(response -> response.bodyToMono(Location.class));
    }

    private void notifyApp(UUID deviceId, Location location) {
        //@formatter:off
        var tokens = userRepository.fetchAllViewers(deviceId);
        var data = Map.of(
            "latitude", String.valueOf(location.latitude()),
            "longitude", String.valueOf(location.longitude()));
        firebaseService.message(tokens, data);
        //@formatter:off
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            log.info("{}: {}", Arrays.toString(token.getTopics()), token.getMessage());
        } catch (MqttException e) {
            log.error("deliveryComplete", e);
        }
    }
}

