package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.model.transfer.LocaleDevice;
import com.viettel.vtag.model.transfer.PlatformData;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import com.viettel.vtag.service.interfaces.MqttPublisher;
import com.viettel.vtag.utils.DeviceConfigSerializer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.viettel.vtag.service.impl.GeoServiceImpl.distance;

@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final DeviceRepository deviceRepository;
    private final IotPlatformService iotPlatformService;
    private final MqttPublisher publisher;
    private final MqttClient subscriber;

    {
        var module = new SimpleModule();
        module.addSerializer(DeviceConfig.class, new DeviceConfigSerializer());
        mapper.registerModule(module);
    }

    @Override
    public Mono<Boolean> pairDevice(User user, PairDeviceRequest request) {
        var uuid = request.platformId();
        return Mono.justOrEmpty(uuid)
            .map(id -> deviceRepository.save(new Device().name("VTAG").platformId(id)))
            .doOnNext(saved -> log.info("saved {}", saved))
            .filter(saved -> saved > 0)

            .flatMap(id -> iotPlatformService.put("/api/devices/" + uuid + "/group/" + user.platformId(), request))
            .doOnNext(response -> log.info("{}: {} {}", uuid, "/group", response.statusCode()))
            .filter(response -> response.statusCode().is2xxSuccessful())

            .flatMap(ok -> iotPlatformService.post("/api/devices/" + uuid + "/active", Map.of("Type", "MAD")))
            .doOnNext(response -> log.info("{}: atv {}", uuid, response.statusCode()))
            .map(response -> response.statusCode().is2xxSuccessful())

            .map(paired -> deviceRepository.setUserDevice(user, request) > 0)
            .doOnNext(paired -> {
                // @formatter:off
                try {
                    subscriber.subscribe(new String[] {
                        "messages/" + uuid + "/data",
                        "messages/" + uuid + "/userdefined/battery",
                        "messages/" + uuid + "/userdefined/wificell",
                        "messages/" + uuid + "/userdefined/devconf"
                    });
                } catch (MqttException e) {
                    log.error("{}: couldn't subscribe {}", uuid, e.getMessage());
                }
                // @formatter:on
            })
            .doOnError(e -> log.error("Error on pairing device: {}", e.getMessage()));
    }

    @Override
    public Mono<Boolean> unpairDevice(User user, PairDeviceRequest request) {
        //@formatter:off
        var uuid = request.platformId();
        return Mono.justOrEmpty(uuid)

            .map(id -> "/api/devices/" + id + "/deactive")
            .flatMap(endpoint -> iotPlatformService.post(endpoint, Map.of("Type", "DAM")))
            .doOnNext(response -> log.info("{}: dtv {}", uuid, response.statusCode()))
            // .map(response -> response.statusCode().is2xxSuccessful())

            .flatMap(endpoint -> iotPlatformService.delete("/api/devices/" + uuid + "/group/" + user.platformId()))
            // .map(response -> response.statusCode().is2xxSuccessful())
            // .filter(unpaired -> unpaired)

            .map(unpaired -> deviceRepository.delete(user, request.platformId()))
            .doOnNext(saved -> log.info("{}: del dvc: rs {}", request.platformId(), saved))
            .map(deleted -> deleted > 0)

            .doOnNext(response -> {
                try {
                    subscriber.unsubscribe(new String[] {
                        "messages/" + uuid + "/data",
                        "messages/" + uuid + "/userdefined/battery",
                        "messages/" + uuid + "/userdefined/wificell",
                        "messages/" + uuid + "/userdefined/devconf"});
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            })
            .filter(deleted -> deleted);
        //@formatter:on
    }

    @Override
    public Mono<List<Device>> getDeviceList(User user) {
        return Mono.just(deviceRepository.getUserDevices(user));
    }

    @Override
    public Mono<Device> getDevice(User user, UUID deviceId) {
        return Mono.justOrEmpty(deviceRepository.getUserDevices(user, deviceId));
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
    public Mono<Integer> updateDeviceName(User user, ChangeDeviceNameRequest detail) {
        return Mono.just(deviceRepository.updateName(user, detail)).filter(updated -> updated > 0);
    }

    @Override
    public Mono<String> getGeoFencing(User user, UUID deviceId) {
        return Mono.justOrEmpty(deviceRepository.getGeoFencing(user, deviceId));
    }

    @Override
    public Mono<Integer> updateGeofencing(User user, UUID deviceId, List<Fence> fence) {
        try {
            return Mono.just(deviceRepository.updateGeoFencing(user, deviceId, fence));
        } catch (Exception e) {
            log.error("error inserting geo-fencing {}", e.getMessage());
            return Mono.empty();
        }
    }

    @Override
    public Mono<Integer> deleteGeofencing(User user, UUID deviceId) {
        return Mono.just(deviceRepository.deleteGeoFencing(user, deviceId))
            .doOnError(e -> log.error("error deleting geo-fencing {}", e.getMessage()));
    }

    @Override
    public Mono<String> getDeviceMessages(User user, UUID deviceId, String topics, int offset, int limit) {
        var endpoint =
            "/api/messages/group/" + user.platformId() + "/selected_topic?deviceId=" + deviceId + "&topic=" + topics
                + "&offset=" + offset + "&limit=" + limit;
        return iotPlatformService.getWithToken(endpoint)
            .doOnNext(response -> {
                if (!response.statusCode().is2xxSuccessful()) {
                    log.info("{}: msg {}", deviceId, response.statusCode());
                }
            })
            .filter(response -> response.statusCode().is2xxSuccessful())
            .flatMap(response -> response.bodyToMono(String.class));
    }

    @Override
    public Mono<DeviceConfig> getConfig(User user, UUID deviceId) {
        return iotPlatformService.get("/messages/custom?deviceId=" + deviceId)
            .doOnNext(response -> {
                var statusCode = response.statusCode();
                if (statusCode.is2xxSuccessful()) {
                    log.info("{}: cfg {}", deviceId, statusCode);
                }
            })
            .filter(response -> response.statusCode().is2xxSuccessful())
            .flatMap(response -> response.bodyToMono(PlatformData.class))
            .map(platformData -> platformData.data().get(0).payload())
            .flatMap(string -> Mono.fromCallable(() -> mapper.readValue(string, DeviceConfig.class)));
    }

    @Override
    public Mono<Boolean> updateConfig(User user, UUID deviceId, DeviceConfig config) {
        return Mono.fromCallable(() -> mapper.writeValueAsBytes(config))
            .doOnNext(payload -> publisher.publish("messages/" + deviceId + "/app/controls", payload, 1, true))
            .thenReturn(true)
            .onErrorReturn(false);
    }

    @Override
    public Mono<List<LocationHistory>> fetchHistory(User user, LocationHistoryRequest detail) {
        return Mono.justOrEmpty(deviceRepository.fetchHistory(user, detail));
    }

    @Override
    public Mono<List<LocaleDevice>> findLocaleDevices(ILocation location, double radius) {
        return Mono.justOrEmpty(deviceRepository.getLocaleDevices(location))
            .map(devices -> devices.stream()
                .filter(d -> distance(d.latitude(), d.longitude(), location.latitude(), location.longitude()) < radius)
                .collect(Collectors.toList()));
    }
}
