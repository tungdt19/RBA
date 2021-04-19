package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.service.interfaces.AdminDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDeviceServiceImpl implements AdminDeviceService {

    @Override
    public Mono<List<Device>> getAllDevices() {
        return null;
    }
}
