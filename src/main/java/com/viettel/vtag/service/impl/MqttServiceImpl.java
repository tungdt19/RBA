package com.viettel.vtag.service.impl;

import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.service.interfaces.MqttService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttServiceImpl implements MqttService {

    private final DeviceRepository deviceRepository;
    private final MqttClient subscriber;

    @PostConstruct
    public void subscribeExistedDevices() {
        //@formatter:off
        var uuids = deviceRepository.fetchAllDevices();
        log.info("Subscribing to topics: {}", uuids);
        for (var uuid : uuids) {
            try {
                subscriber.subscribe(new String[] {
                    "messages/" + uuid + "/data",
                    "messages/" + uuid + "/userdefined/battery",
                    "messages/" + uuid + "/userdefined/wificell",
                    "messages/" + uuid + "/userdefined/devconf"
                });
            } catch (MqttException e) {
                log.error("Cannot subscribe to uuid {}: {}", uuid, e.getMessage());
            }
        }
        //@formatter:on
    }
}
