package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface AdminDeviceService {

    Mono<List<Device>> getAllDevices();

    Mono<List<LocationHistory>> getDeviceHistory(UUID device);

    Mono<List<String>> getAllViewerTokens(UUID deviceId);
}
