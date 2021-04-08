package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.model.transfer.BatteryMessage;
import com.viettel.vtag.model.transfer.ConfigMessage;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DeviceRepositoryImpl implements DeviceRepository {

    private final JdbcTemplate jdbc;

    @Override
    public List<UUID> fetchAllDevices() {
        var sql = "SELECT platform_device_id FROM device";
        return jdbc.query(sql, (rs, rowNum) -> rs.getObject("platform_device_id", UUID.class));
    }

    @Override
    public Device get(int id) {
        var sql = "SELECT id, name, imei FROM device WHERE id = ?";
        return jdbc.queryForObject(sql, new Object[] {id}, this::parseDevice);
    }

    private Device parseDevice(ResultSet rs, int i) throws SQLException {
        return new Device().id(rs.getInt("id")).name(rs.getString("name")).imei(rs.getString("imei"));
    }

    @Override
    public Device find(String imei) {
        var sql = "SELECT id, name, imei FROM device WHERE imei = ?";
        return jdbc.queryForObject(sql, new Object[] {imei}, this::parseDevice);
    }

    @Override
    public int save(Device device) {
        var sql = "INSERT INTO device(name, platform_device_id) VALUES (?, ?)";
        return jdbc.update(sql, device.name(), device.platformId());
    }

    @Override
    public int update(Device device) {
        var sql = "UPDATE device SET name = ?, imei = ? WHERE id = ?";
        return jdbc.update(sql, device.name(), device.imei(), device.id());
    }

    @Override
    public int updateName(User user, ChangeDeviceNameRequest request) {
        var sql = "UPDATE device d SET name = ? FROM user_role ur WHERE ur.device_id = d.id AND ur.user_id = ? "
            + "AND role_id = 1 AND device_id = ?";
        return jdbc.update(sql, request.name(), user.id(), request.platformId());
    }

    @Override
    public int addViewer(User user, AddViewerRequest request) {
        var sql = "INSERT INTO user_role(device_id, user_id, role_id) SELECT d.id, ?, ? FROM device d "
            + "WHERE platform_device_id = ?";
        return jdbc.update(sql, request.phone(), 2, request.deviceId());
    }

    @Override
    public int removeViewer(User user, RemoveViewerRequest request) {
        var sql = "DELETE FROM user_role ur USING device d WHERE d.id = ur.device_id AND ur.user_id = ? AND role_id = 1"
            + " AND platform_device_id = ?";
        return jdbc.update(sql, user.id(), request.deviceId(), "ROLE_VIEWER");
    }

    @Override
    public int delete(Device device) {
        var sql = "DELETE FROM device WHERE id = ?";
        return jdbc.update(sql, device.id());
    }

    @Override
    public List<Device> getUserDevice(User user) {
        var sql = "SELECT id, name, imei, platform_device_id, battery FROM device "
            + "JOIN user_role ur ON device.id = ur.device_id WHERE user_id = ?";
        return jdbc.query(sql, new Object[] {user.id()}, this::mapDevice);
    }

    @Override
    public int setUserDevice(User user, PairDeviceRequest request) {
        var sql = "INSERT INTO user_role (device_id, user_id, role_id) SELECT d.id, ?, ? FROM device d "
            + "WHERE platform_device_id = ?";
        return jdbc.update(sql, user.id(), 1, request.platformId());
    }

    @Override
    public int updateBattery(UUID platformDeviceId, BatteryMessage battery) {
        var sql = "UPDATE device SET battery = ? WHERE platform_device_id = ?";
        return jdbc.update(sql, battery.level(), platformDeviceId);
    }

    @Override
    public int updateConfig(UUID platformDeviceId, ConfigMessage config) {
        var sql = "UPDATE device SET status = ? WHERE platform_device_id = ?";
        return jdbc.update(sql, config.MMC().modeString(), platformDeviceId);
    }

    private Device mapDevice(ResultSet rs, int i) throws SQLException {
        return new Device().id(rs.getInt("id"))
            .name(rs.getString("name"))
            .imei(rs.getString("imei"))
            .battery(rs.getInt("battery"))
            .platformId(rs.getObject("platform_device_id", UUID.class));
    }
}
