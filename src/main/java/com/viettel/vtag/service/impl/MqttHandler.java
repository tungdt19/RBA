package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.Notification;
import com.viettel.vtag.config.MqttSubscriberConfig;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Location;
import com.viettel.vtag.model.transfer.*;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.repository.interfaces.LocationHistoryRepository;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.FirebaseService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.*;

/** @see MqttSubscriberConfig#mqttSubscriberClient(MqttCallback) */
@Slf4j
@Service
public class MqttHandler implements MqttCallback {

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient httpClient;
    private final DeviceRepository deviceRepository;
    private final FirebaseService firebaseService;
    private final UserRepository userRepository;
    private final LocationHistoryRepository locationHistory;
    private final MessageSource messageSource;
    private final MqttClient publisher;
    private final String convertAddress;
    private final String convertUri;
    private final String convertToken;

    public MqttHandler(
        HttpClient httpClient,
        DeviceRepository deviceRepository,
        FirebaseService firebaseService,
        UserRepository userRepository,
        LocationHistoryRepository locationHistory,
        MessageSource messageSource,
        @Qualifier("mqtt-publisher-client") MqttClient publisher,
        @Value("${vtag.unwired.base-url}") String convertAddress,
        @Value("${vtag.unwired.uri}") String convertUri,
        @Value("${vtag.unwired.token}") String convertToken
    ) {
        this.httpClient = httpClient;
        this.deviceRepository = deviceRepository;
        this.firebaseService = firebaseService;
        this.userRepository = userRepository;
        this.locationHistory = locationHistory;
        this.messageSource = messageSource;
        this.publisher = publisher;
        this.convertAddress = convertAddress;
        this.convertUri = convertUri;
        this.convertToken = convertToken;
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
        log.info("Received an GPS message: {}", payload);
        var gps = mapper.readValue(payload, LocationMessage.class);
        if ("DPOS".equals(gps.type())) {
            locationHistory.save(deviceId, gps);
        }
    }

    private void handleWifiCellMessage(UUID deviceId, String payload) throws JsonProcessingException {
        var data = mapper.readValue(payload, CellWifiMessage.class);

        switch (data.type()) {
            case "DSOS":
                convertWifiCell(deviceId, data).subscribe(locationMessage -> notifyApp(deviceId, locationMessage));
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
        var updated = deviceRepository.updateBattery(deviceId, data);
        if (updated > 0) {
            log.info("Updated battery info ({}) for {}", data.level(), deviceId);
        }
    }

    private void updateConfig(UUID deviceId, String payload) {
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

    private Mono<LocationMessage> convertWifiCell(UUID deviceId, CellWifiMessage payload) {
        log.info("Converting wificell info from {}: {}", deviceId, payload.type());
        return convert(deviceId, payload).doOnNext(
            location -> log.info("Converted message from {}: {}", deviceId, location))
            .doOnNext(location -> locationHistory.save(deviceId, location))
            .map(LocationMessage::fromLocation)
            .doOnNext(location -> {
                var topic = "messages/" + deviceId + "/data";
                try {
                    var bytes = new MqttMessage();
                    bytes.setPayload(mapper.writeValueAsBytes(location));
                    publisher.publish(topic, bytes);
                } catch (MqttException | JsonProcessingException e) {
                    log.error("Couldn't publish location {} to topic '{}'", location, topic, e);
                }
            })
            .doOnError(e -> log.error("Error converting", e));
    }

    private void notifyApp(UUID deviceId, ILocation location) {
        //@formatter:off
        var notification= Notification.builder()
            .setTitle(messageSource.getMessage("message.sos.title", new Object[] {}, Locale.ENGLISH))
            .setBody(messageSource.getMessage("message.sos.content", new Object[] {}, Locale.ENGLISH))
            .build();
        var tokens = userRepository.fetchAllViewers(deviceId);
        var data = Map.of(
            "latitude", String.valueOf(location.latitude()),
            "longitude", String.valueOf(location.longitude()));
        firebaseService.message(tokens, notification, data);
        //@formatter:off
    }

    public Mono<Location> convert(UUID deviceId, CellWifiMessage json) {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(convertAddress)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(convertUri)
            .bodyValue(json.token(convertToken))
            .exchange()
            .doOnNext(response -> log.info("converted {} -> {}", deviceId, response.statusCode()))
            .filter(response -> response.statusCode().is2xxSuccessful())
            .flatMap(response -> response.bodyToMono(Location.class))
            .doOnNext(location -> log.info("location {}: {}", deviceId, location));
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

