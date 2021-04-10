package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.model.transfer.BatteryMessage;
import com.viettel.vtag.model.transfer.ConfigMessage;

import java.util.List;
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

    int delete(User user, UUID platformId);

    List<LocationHistory> fetchHistory(User user, LocationHistoryRequest request);
}
