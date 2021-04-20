package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface DeviceService {

    Mono<Boolean> pairDevice(User user, PairDeviceRequest request);

    Mono<Integer> saveUserDevice(User user, PairDeviceRequest request);

    Mono<List<Device>> getDeviceList(User user);

    Mono<Device> getDevice(User user, UUID deviceId);

    Mono<Integer> addViewer(User user, AddViewerRequest deviceId);

    Mono<Integer> removeViewer(User user, RemoveViewerRequest detail);

    Mono<Integer> updateDeviceName(User user, ChangeDeviceNameRequest request);

    Mono<String> getGeoFencing(User user, UUID deviceId);

    Mono<Integer> updateGeofencing(User user, UUID deviceId, List<Fence> fence);

    Mono<Integer> deleteGeofencing(User user, UUID deviceId);

    default Mono<String> getDeviceMessages(User user, UUID deviceId, int offset, int limit) {
        return getDeviceMessages(user, deviceId, "data,battery,wificell,devconf", offset, limit);
    }

    Mono<String> getDeviceMessages(User user, UUID deviceId, String topics, int offset, int limit);

    Mono<DeviceConfig> getConfig(User user, UUID deviceId);

    Mono<Integer> updateConfig(User user, UUID deviceId, DeviceConfig config);

    Mono<List<LocationHistory>> fetchHistory(User user, LocationHistoryRequest request);

    Mono<Boolean> unpairDevice(User user, PairDeviceRequest request);

    Mono<Boolean> removeUserDevice(User user, PairDeviceRequest request);
}
