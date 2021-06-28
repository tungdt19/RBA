package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.transfer.BatteryMessage;
import com.viettel.vtag.model.transfer.ConfigMessage;
import com.viettel.vtag.model.transfer.WifiCellMessage;
import com.viettel.vtag.repository.interfaces.DeviceMessageRepository;
import com.viettel.vtag.service.interfaces.DeviceMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceMessageServiceImpl implements DeviceMessageService {

    private final DeviceMessageRepository deviceMessageRepository;

    @Override
    public Mono<Integer> updateLocation(UUID deviceId, ILocation location, Integer battery, Long timestamp) {
        return Mono.just(deviceMessageRepository.saveLocation(deviceId, location, battery, timestamp));
    }

    @Override
    public Mono<Integer> updateBattery(UUID deviceId, BatteryMessage data) {
        return Mono.just(deviceMessageRepository.updateBattery(deviceId, data));
    }

    @Override
    public Mono<Integer> updateConfig(UUID deviceId, ConfigMessage data) {
        return Mono.just(deviceMessageRepository.updateConfig(deviceId, data));
    }
}
