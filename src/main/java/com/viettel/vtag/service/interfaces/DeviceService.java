package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DeviceService {

    Mono<ClientResponse> pairDevice(User user, PairDeviceRequest request);

    Mono<Integer> saveUserDevice(User user, PairDeviceRequest request);

    Mono<Integer> unpairDevice(User user, PairDeviceRequest request);

    Mono<Boolean> deactivate(PairDeviceRequest request);

    Mono<Integer> updateDeviceName(User user, ChangeDeviceNameRequest request);

    Mono<Integer> addViewer(User user, AddViewerRequest deviceId);

    Mono<Integer> removeViewer(User user, RemoveViewerRequest detail);

    Mono<Integer> insertGeofencing(User user, UUID uuid, Fencing fencing);

    Mono<Integer> insertGeofencing(User user, UUID uuid, Map<String, Fencing> fencing);

    Mono<Integer> updateGeofencing(User user, UUID uuid, Fencing fencing);

    Mono<Integer> updateGeofencing(User t1, UUID t2, Map<String, Fencing> fencing);

    Mono<Integer> deleteGeofencing(User user, UUID uuid, String fencing);

    Mono<List<Device>> getList(User user);

    Mono<List<LocationHistory>> fetchHistory(User user, LocationHistoryRequest request);

    Mono<ClientResponse> getMessages(User user, UUID deviceId, int offset, int limit);

    Mono<Device> getGeofencing(User user, UUID deviceId);
}
