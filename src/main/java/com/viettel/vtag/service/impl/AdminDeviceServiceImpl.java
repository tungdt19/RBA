package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.repository.interfaces.AdminDeviceRepository;
import com.viettel.vtag.service.interfaces.AdminDeviceService;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDeviceServiceImpl implements AdminDeviceService {

    private final IotPlatformService iotPlatformService;
    private final AdminDeviceRepository deviceRepository;

    @Override
    public Mono<List<Device>> getAllDevices() {
        return Mono.justOrEmpty(deviceRepository.getAllDevices());
    }

    @Override
    public Mono<List<LocationHistory>> getDeviceHistory(UUID device) {
        return Mono.justOrEmpty(deviceRepository.getDeviceHistory(device));
    }

    @Override
    public Mono<List<String>> getDeviceNotificationTokens(UUID deviceId) {
        return Mono.justOrEmpty(deviceRepository.getAllViewerTokens(deviceId));
    }

    @Override
    public Mono<String> getAllPlatformDevices() {
        return iotPlatformService.get("/api/devices")
            .doOnNext(response -> log.info("all devices {}", response.statusCode()))
            .flatMap(response -> response.bodyToMono(String.class));
    }
}
