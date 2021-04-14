package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.transfer.BatteryMessage;
import com.viettel.vtag.model.transfer.ConfigMessage;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeviceMessageService {

    Mono<Integer> updateLocation(UUID deviceId, ILocation gps);

    Mono<Integer> updateBattery(UUID deviceId, BatteryMessage data);

    Mono<Integer> updateConfig(UUID deviceId, ConfigMessage data);
}
