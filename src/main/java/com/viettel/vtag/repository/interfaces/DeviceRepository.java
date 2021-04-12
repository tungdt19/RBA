package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DeviceRepository {

    Device get(int id);

    Device find(UUID platformId);

    int save(Device device);

    int update(Device device);

    int updateName(User user, ChangeDeviceNameRequest device);

    int addViewer(User user, AddViewerRequest request);

    int removeViewer(User user, RemoveViewerRequest request);

    int removeUserDevice(User user, PairDeviceRequest request);

    List<UUID> fetchAllDevices();

    List<Device> getUserDevice(User user);

    int setUserDevice(User user, PairDeviceRequest request);

    List<LocationHistory> fetchHistory(User user, LocationHistoryRequest request);

    int insertGeoFencing(User user, UUID deviceId, Fencing fencing);

    int insertGeoFencing(User user, UUID deviceId, Map<String, Fencing> fencing);

    int updateGeoFencing(User user, UUID deviceId, Fencing fencing);

    int updateGeoFencing(User user, UUID deviceId, Map<String, Fencing> fencing);

    int deleteGeoFencing(User user, UUID deviceId, String name);

    int delete(User user, UUID platformId);
}
