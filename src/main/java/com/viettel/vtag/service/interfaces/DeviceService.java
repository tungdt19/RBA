package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeviceService {

    int addViewer(User user, AddViewerRequest deviceId);

    String getInfo(String deviceId);

    List<Device> getList(User user);

    int remove(User user, RemoveViewerRequest detail);

    Mono<ClientResponse> convert(String json);

    Mono<ClientResponse> convert(PlatformData json);

    Mono<Integer> pairDevice(User user, PairDeviceRequest request);

    List<LocationHistory> fetchHistory(User user, LocationHistoryRequest detail);

    Mono<ClientResponse> active(PairDeviceRequest detail);
}
