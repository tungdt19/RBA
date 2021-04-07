package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeviceService {

    Mono<Boolean> activate(PairDeviceRequest detail);

    Mono<Integer> pairDevice(User user, PairDeviceRequest request);

    Mono<Integer> updateDeviceName(User user, ChangeDeviceNameRequest detail);

    Mono<Integer> addViewer(User user, AddViewerRequest deviceId);

    Mono<Integer> removeViewer(User user, RemoveViewerRequest detail);

    Mono<List<Device>> getList(User user);

    Mono<List<LocationHistory>> fetchHistory(User user, LocationHistoryRequest detail);
}
