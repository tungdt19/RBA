package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.Device;

public interface DeviceRepository {

    Device get(int id);

    Device find(String imei);

    int save(Device device);

    int update(Device device);

    int delete(Device device);
}
