package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.Device;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AdminDeviceService {
    Mono<List<Device>> getAllDevices();
}
