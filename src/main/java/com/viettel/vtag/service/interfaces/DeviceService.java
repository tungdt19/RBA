package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface DeviceService {

    Mono<Integer> pairDevice(User user, PairDeviceRequest request);

    Mono<Boolean> activate(PairDeviceRequest request);

    Mono<Integer> unpairDevice(User user, PairDeviceRequest request);

    Mono<Boolean> deactivate(PairDeviceRequest request);

    Mono<Integer> updateDeviceName(User user, ChangeDeviceNameRequest request);

    Mono<Integer> addViewer(User user, AddViewerRequest deviceId);

    Mono<Integer> removeViewer(User user, RemoveViewerRequest detail);

    Mono<List<Device>> getList(User user);

    Mono<List<LocationHistory>> fetchHistory(User user, LocationHistoryRequest request);

    Mono<ClientResponse> getMessages(User user, UUID deviceId, int offset, int limit);
}
