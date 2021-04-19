package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.repository.interfaces.AdminDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminDeviceRepositoryImpl implements AdminDeviceRepository {

    @Override
    public List<Device> getAllDevices() {
        return null;
    }

    @Override
    public List<String> getAllViewerTokens(UUID platformId) {
        return null;
    }

    @Override
    public List<LocationHistory> getDeviceHistory(UUID device) {
        return null;
    }
}
