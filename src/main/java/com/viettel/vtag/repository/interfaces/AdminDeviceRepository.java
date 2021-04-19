package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;

import java.util.List;
import java.util.UUID;

public interface AdminDeviceRepository {

    List<Device> getAllDevices();

    List<String> getAllViewerTokens(UUID platformId);

    List<LocationHistory> getDeviceHistory(UUID device);
}
