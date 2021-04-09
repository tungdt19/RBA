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
import com.viettel.vtag.service.interfaces.MqttService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final DeviceRepository deviceRepository;
    private final IotPlatformService iotPlatformService;
    private final LocationHistoryRepository locationHistory;
    private final MqttService mqttService;

    @Override
    public Mono<Integer> pairDevice(User user, PairDeviceRequest request) {
        var endpoint = "/api/devices/" + request.platformId() + "/group/" + user.platformId();
        return iotPlatformService.put(endpoint, request)
            .filter(response -> response.statusCode().is2xxSuccessful())
            .doOnNext(response -> log.info("{}: {}", endpoint, response.statusCode()))
            .map(response -> deviceRepository.save(new Device().name("VTAG").platformId(request.platformId())))
            .doOnNext(saved -> log.info("saved {}", saved))
            .filter(paired -> paired > 0)
            .map(paired -> deviceRepository.setUserDevice(user, request))
            .doOnNext(saved -> log.info("save user device {}", saved));
    }

    @Override
    public Mono<Boolean> activate(PairDeviceRequest request) {
        // @formatter:off
        var uuid = request.platformId();
        return Mono.justOrEmpty(uuid)
            .map(id -> "/api/devices/" + id + "/active")
            .flatMap(endpoint -> iotPlatformService.post(endpoint, Map.of("Type", "MAD")))
            .doOnNext(response -> log.info("{}: {}", uuid, response.statusCode()))
            .map(response -> response.statusCode().is2xxSuccessful())
            .filter(paired -> paired)
            .doOnNext(response -> mqttService.subscribe(new String[] {
                "messages/" + uuid + "/data",
                "messages/" + uuid + "/userdefined/battery",
                "messages/" + uuid + "/userdefined/wificell",
                "messages/" + uuid + "/userdefined/devconf"}));
        // @formatter:on
    }

    @Override
    public Mono<Integer> unpairDevice(User user, PairDeviceRequest request) {
        var endpoint = "/api/devices/" + request.platformId() + "/group/" + user.platformId();
        return iotPlatformService.delete(endpoint)
            .filter(response -> response.statusCode().is2xxSuccessful())
            .doOnNext(response -> log.info("{}: {}", endpoint, response.statusCode()))
            .map(response -> deviceRepository.delete(user, request.platformId()))
            .doOnNext(saved -> log.info("saved {}", saved))
            .filter(paired -> paired > 0)
            .map(paired -> deviceRepository.setUserDevice(user, request))
            .doOnNext(saved -> log.info("save user device {}", saved));
    }

    @Override
    public Mono<Boolean> deactivate(PairDeviceRequest request) {
        //@formatter:off
        var uuid = request.platformId();
        return Mono.justOrEmpty(uuid)
            .map(id -> "/api/devices/" + id + "/deactive")
            .flatMap(endpoint -> iotPlatformService.post(endpoint, Map.of("Type", "DAM")))
            .doOnNext(response -> log.info("{}: {}", uuid, response.statusCode()))
            .map(response -> response.statusCode().is2xxSuccessful())
            .filter(paired -> paired)
            .doOnNext(response -> mqttService.unsubscribe(new String[] {
                "messages/" + uuid + "/data",
                "messages/" + uuid + "/userdefined/battery",
                "messages/" + uuid + "/userdefined/wificell",
                "messages/" + uuid + "/userdefined/devconf"}));
        //@formatter:on
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
        return Mono.justOrEmpty(locationHistory.fetch(user, detail));
    }

    @Override
    public Mono<ClientResponse> getMessages(User user, UUID deviceId, int offset, int limit) {
        var endpoint = "/api/messages/group/" + user.platformId() + "/selected_topic?deviceId=" + deviceId
            + "&topic=data,battery,wificell&offset=" + offset + "&limit=" + limit;
        log.info("msg endpoint {}", endpoint);
        return iotPlatformService.getWithToken(endpoint);
    }
}
