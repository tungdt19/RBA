package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.repository.interfaces.LocationHistoryRepository;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.slf4j.helpers.MessageFormatter.format;

@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final DeviceRepository deviceRepository;
    private final IotPlatformService iotPlatformService;
    private final LocationHistoryRepository locationHistory;

    @Override
    public Mono<Boolean> activate(PairDeviceRequest request) {
        var endpoint = format("/api/devices/{}/active", request.platformId()).getMessage();
        log.info(endpoint);
        return iotPlatformService.post(endpoint, Map.of("Type", "MAD"))
            .map(response -> response.statusCode().is2xxSuccessful())
            .filter(activated -> activated);
    }

    @Override
    public Mono<Integer> pairDevice(User user, PairDeviceRequest request) {
        var endpoint = format("/api/devices/{}/group/{}", request.platformId(), user.platformId()).getMessage();
        return iotPlatformService.put(endpoint, request)
            .filter(response -> response.statusCode().is2xxSuccessful())
            .map(response -> deviceRepository.save(new Device().name("VTAG").platformId(request.platformId())))
            .filter(paired -> paired > 0)
            .flatMap(paired -> Mono.just(deviceRepository.setUserDevice(user, request)));
    }

    @Override
    public Mono<Integer> updateDeviceName(User user, ChangeDeviceNameRequest detail) {
        return Mono.just(deviceRepository.updateName(user, detail)).filter(updated -> updated > 0);
    }

    @Override
    public Mono<Integer> addViewer(User user, AddViewerRequest request) {
        return Mono.just(deviceRepository.addViewer(user, request)).filter(added -> added > 0);
    }

    @Override
    public Mono<Integer> removeViewer(User user, RemoveViewerRequest request) {
        return Mono.just(deviceRepository.removeViewer(user, request)).filter(removed -> removed > 0);
    }

    @Override
    public Mono<List<Device>> getList(User user) {
        return Mono.just(deviceRepository.getUserDevice(user));
    }

    @Override
    public Mono<List<LocationHistory>> fetchHistory(User user, LocationHistoryRequest detail) {
        return Mono.justOrEmpty(locationHistory.fetch(user, detail)).filter(locations -> locations.size() > 0);
    }
}
