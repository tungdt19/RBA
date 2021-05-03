package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository {

    Device get(UUID platformId);

    int save(Device device);

    int update(Device device);

    int updateName(User user, ChangeDeviceNameRequest device);

    int addViewer(User user, AddViewerRequest request);

    int removeViewer(User user, RemoveViewerRequest request);

    int removeUserDevice(User user, UUID deviceId);

    List<Device> getAllDevices();

    List<UUID> getAllPlatformId();

    List<Device> getUserDevices(User user);

    Device getUserDevices(User user, UUID deviceId);

    List<Device> getLocaleDevices(ILocation location);

    String getGeoFencing(User user, UUID deviceId);

    int updateGeoFencing(User user, UUID deviceId, List<Fence> fence);

    int deleteGeoFencing(User user, UUID deviceId);

    int setUserDevice(User user, PairDeviceRequest request);

    List<LocationHistory> fetchHistory(User user, LocationHistoryRequest request);

    int delete(User user, UUID platformId);
}
