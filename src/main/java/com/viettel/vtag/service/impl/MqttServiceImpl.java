package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.config.MqttConfig;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.model.entity.PlatformData;
import com.viettel.vtag.repository.interfaces.LocationHistoryRepository;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

/** @see MqttConfig#mqttClient(MqttCallback) */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqttServiceImpl implements MqttCallback {

    private final ObjectMapper mapper = new ObjectMapper();

    private final DeviceService deviceService;
    private final FirebaseService firebaseService;
    private final UserRepository userRepository;
    private final LocationHistoryRepository locationHistory;

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.error("MQTT connection lost!", cause);
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            var index = topic.indexOf('/', 9);
            var deviceId = topic.substring(9, index);
            var subtopic = topic.substring(index + 1);
            var payload = new String(message.getPayload());
            var data = mapper.readValue(payload, PlatformData.class);

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
            e.printStackTrace();
        }
    }

    private void handleSosMessage(String subtopic, String deviceId, PlatformData payload) {
        log.info("Received an SOS message: {}", payload);
        var locationMono = deviceService.convert(payload).flatMap(response -> {
            if (response.statusCode() != HttpStatus.OK) {
                return Mono.empty();
            }

            return response.bodyToMono(LocationHistory.class);
        });

        locationMono.subscribe(o -> notifyApp(deviceId, o));
        locationMono.subscribe(locationHistory::save);
    }

    private void handleGpsMessage(String subtopic, String deviceId, PlatformData payload) {
        log.info("Received an GPS message: {}", payload);
    }

    private void handleWifiMessage(String subtopic, String deviceId, PlatformData payload) {
        log.info("Received an Wifi message: {}", payload);
        var location = deviceService.convert(payload);
    }

    private void notifyApp(String deviceId, LocationHistory location) {
        var tokens = userRepository.fetchAllViewers(deviceId);
        firebaseService.message(tokens,
            Map.of("latitude", String.valueOf(location.latitude()), "longitude", String.valueOf(location.longitude())));
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            System.out.println(token.getMessage());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

