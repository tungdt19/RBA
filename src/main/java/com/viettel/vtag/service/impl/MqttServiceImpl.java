package com.viettel.vtag.service.impl;

import com.viettel.vtag.config.MqttConfig;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.util.Map;

/** @see MqttConfig#mqttClient(MqttCallback) */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqttServiceImpl implements MqttCallback {

    private final DeviceService deviceService;
    private final FirebaseService firebaseService;
    private final UserRepository userRepository;

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        var payload = new String(message.getPayload());
        log.info("[{}] {}", topic, payload);
        deviceService.convert(payload);
    }

    private void notifyApp() {
        var tokens = userRepository.fetchAllViewers("");
        firebaseService.message(tokens, Map.of());
    }

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.error("MQTT connection lost!", cause);
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

