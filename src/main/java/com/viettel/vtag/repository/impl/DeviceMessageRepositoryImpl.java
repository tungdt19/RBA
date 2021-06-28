package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.transfer.BatteryMessage;
import com.viettel.vtag.model.transfer.ConfigMessage;
import com.viettel.vtag.repository.cache.DeviceCache;
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
    private final DeviceCache deviceCache;

    @Override
    public int updateBattery(UUID platformDeviceId, BatteryMessage battery) {
        var sql = "UPDATE device SET battery = ? WHERE platform_device_id = ?";
        return jdbc.update(sql, battery.level(), platformDeviceId);
    }

    @Override
    public int updateConfig(UUID platformDeviceId, ConfigMessage config) {
        try {
            var sql = "UPDATE device SET status = ? WHERE platform_device_id = ?";
            return jdbc.update(sql, ConfigMessage.mode(config), platformDeviceId);
        } catch (Exception e) {
            log.error("updateConfig", e);
            return 0;
        }
    }

    @Override
    public int saveLocation(UUID platformDeviceId, ILocation location, Integer battery, Long timestamp) {
        try {
            var update = "UPDATE device SET last_lat = ?, last_lon = ?, accuracy = ?, update_instant = NOW(), "
                + "battery = ? WHERE platform_device_id = ?";

            return jdbc.update(update, location.latitude(), location.longitude(), location.accuracy(), battery,
                platformDeviceId);
        } catch (Exception e) {
            log.error("{}: save location {}", platformDeviceId, location, e);
            return 0;
        }
    }
}
