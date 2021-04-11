package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.transfer.BatteryMessage;
import com.viettel.vtag.model.transfer.ConfigMessage;
import com.viettel.vtag.repository.interfaces.DeviceMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DeviceMessageRepositoryImpl implements DeviceMessageRepository {

    private final JdbcTemplate jdbc;

    @Override
    public int saveLocation(UUID platformDeviceId, ILocation location) {
        var sql = "INSERT INTO location_history(device_id, latitude, longitude, accuracy, trigger_instant) "
            + "SELECT d.id, ?, ?, ?, NOW() FROM device d WHERE d.platform_device_id = ?";
        return jdbc.update(sql, location.latitude(), location.longitude(), location.accuracy(), platformDeviceId);
    }

    @Override
    public int updateBattery(UUID platformDeviceId, BatteryMessage battery) {
        var sql = "UPDATE device SET battery = ? WHERE platform_device_id = ?";
        return jdbc.update(sql, battery.level(), platformDeviceId);
    }

    @Override
    public int updateConfig(UUID platformDeviceId, ConfigMessage config) {
        try {
            var sql = "UPDATE device SET status = ? WHERE platform_device_id = ?";
            log.info("Device {}; MMC {}", platformDeviceId, config.MMC());
            log.info("MMC mode {}", config.MMC().modeString());
            return jdbc.update(sql, config.MMC().modeString(), platformDeviceId);
        } catch (Exception e) {
            log.error("updateConfig", e);
            return 0;
        }
    }
}