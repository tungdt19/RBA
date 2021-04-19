package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.repository.interfaces.AdminDeviceRepository;
import com.viettel.vtag.service.interfaces.AdminDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDeviceServiceImpl implements AdminDeviceService {

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
    public Mono<List<String>> getAllViewerTokens(UUID deviceId) {
        return Mono.justOrEmpty(deviceRepository.getAllViewerTokens(deviceId));
    }
}
