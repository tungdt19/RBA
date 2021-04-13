package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface DeviceService {

    Mono<ClientResponse> pairDevice(User user, PairDeviceRequest request);

    Mono<Integer> saveUserDevice(User user, PairDeviceRequest request);

    Mono<Device> getDevice(User user, UUID deviceId);

    Mono<String> getGeoFencing(User user, UUID deviceId);

    Mono<List<Device>> getDeviceList(User user);

    Mono<Integer> addViewer(User user, AddViewerRequest deviceId);

    Mono<Integer> removeViewer(User user, RemoveViewerRequest detail);

    Mono<Integer> updateDeviceName(User user, ChangeDeviceNameRequest request);

    Mono<Integer> updateGeofencing(User t1, UUID t2, List<Fence> fence);

    Mono<Integer> deleteGeofencing(User user, UUID uuid);

    Mono<String> getMessages(User user, UUID deviceId, int offset, int limit);

    Mono<Integer> updateConfig(User user, UUID deviceId, ConfigRequest config);

    Mono<List<LocationHistory>> fetchHistory(User user, LocationHistoryRequest request);

    Mono<Boolean> unpairDevice(User user, PairDeviceRequest request);

    Mono<Boolean> removeUserDevice(User user, PairDeviceRequest request);
}
