package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.transfer.BatteryMessage;
import com.viettel.vtag.model.transfer.ConfigMessage;

import java.util.UUID;

public interface DeviceMessageRepository {

    int updateBattery(UUID platformDeviceId, BatteryMessage battery);

    int updateConfig(UUID platformDeviceId, ConfigMessage config);

    int saveLocation(UUID platformDeviceId, ILocation location, Integer battery, Long timestamp);
}
