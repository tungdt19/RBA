package com.viettel.vtag.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;
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

    private final ObjectMapper mapper = new ObjectMapper();

    private final JdbcTemplate jdbc;

    @Override
    public Device get(int id) {
        var sql = "SELECT id, name, imei, platform_device_id, battery, status, geo_length FROM device WHERE id = ?";
        return jdbc.queryForObject(sql, new Object[] {id}, (rs, i) -> parseDevice(rs));
    }

    @Override
    public Device find(UUID platformId) {
        var sql = "SELECT id, name, imei, platform_device_id, battery, status FROM device WHERE platform_device_id = ?";
        return jdbc.queryForObject(sql, new Object[] {platformId}, this::parseDevice);
    }

    private Device parseDevice(ResultSet rs, int i) throws SQLException {
        return new Device().id(rs.getInt("id"))
            .name(rs.getString("name"))
            .imei(rs.getString("imei"))
            .battery(rs.getInt("battery"))
            .status(rs.getString("status"))
            .platformId(rs.getObject("platform_device_id", UUID.class));
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
        var sql = "UPDATE device d SET name = ? FROM user_role ur WHERE d.id = ur.device_id AND ur.user_id = ? "
            + "AND role_id = 1 AND d.platform_device_id = ?";
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
        return jdbc.update(sql, user.id(), request.deviceId());
    }

    @Override
    public int removeUserDevice(User user, UUID deviceId) {
        var sql = "DELETE FROM user_role ur USING device d WHERE d.id = ur.device_id AND ur.user_id = ? AND role_id = 1"
            + " AND platform_device_id = ?";
        return jdbc.update(sql, user.id(), deviceId);
    }

    @Override
    public List<UUID> fetchAllDevices() {
        var sql = "SELECT platform_device_id FROM device WHERE platform_device_id IS NOT NULL";
        return jdbc.query(sql, (rs, rowNum) -> rs.getObject("platform_device_id", UUID.class));
    }

    @Override
    public List<Device> getUserDevice(User user) {
        var sql = "SELECT id, name, imei, platform_device_id, battery, status, geo_length, geo_fencing, update_instant,"
            + " last_lat, last_lon FROM device INNER JOIN user_role ur ON device.id = ur.device_id WHERE user_id = ?";
        return jdbc.query(sql, new Object[] {user.id()}, (rs, i) -> parseDevice(rs));
    }

    @Override
    public Device getUserDevice(User user, UUID deviceId) {
        var sql = "SELECT id, name, imei, platform_device_id, battery, status, geo_length, geo_fencing, last_lat, "
            + "last_lon, update_instant FROM device INNER JOIN user_role ur ON device.id = ur.device_id "
            + "WHERE user_id = ? AND platform_device_id = ?";
        return jdbc.queryForObject(sql, new Object[] {user.id(), deviceId}, (rs, i) -> parseDevice(rs));
    }

    @Override
    public String getDeviceGeo(User user, UUID deviceId) {
        var sql = "SELECT geo_fencing FROM device INNER JOIN user_role ur ON id = ur.device_id WHERE user_id = ? "
            + "AND platform_device_id = ?";
        return jdbc.queryForObject(sql, new Object[] {user.id(), deviceId}, (rs, i) -> rs.getString("geo_fencing"));
    }

    @Override
    public int setUserDevice(User user, PairDeviceRequest request) {
        var sql = "INSERT INTO user_role (device_id, user_id, role_id) SELECT d.id, ?, ? FROM device d "
            + "WHERE platform_device_id = ?";
        return jdbc.update(sql, user.id(), 1, request.platformId());
    }

    @Override
    public List<LocationHistory> fetchHistory(User user, LocationHistoryRequest request) {
        var sql = "SELECT latitude, longitude, trigger_instant FROM location_history lh JOIN user_role ur "
            + "ON lh.device_id = ur.device_id JOIN device d ON d.id = ur.device_id WHERE ur.user_id = ? "
            + "AND trigger_instant > ? AND trigger_instant < ? AND platform_device_id = ? ORDER BY trigger_instant";
        return jdbc.query(sql, new Object[] {user.id(), request.from(), request.to(), request.deviceId()},
            (rs, num) -> new LocationHistory().latitude(rs.getDouble("latitude"))
                .longitude(rs.getDouble("longitude"))
                .time(rs.getTimestamp("trigger_instant").toLocalDateTime()));
    }

    @Override
    public int updateGeoFencing(User user, UUID deviceId, List<Fence> fence) {
        try {
            var sql = "UPDATE device d SET geo_fencing = ?::JSONB, geo_length = ? FROM user_role ur "
                + "WHERE ur.device_id = d.id AND ur.user_id = ? AND platform_device_id = ?";
            return jdbc.update(sql, mapper.writeValueAsString(fence), fence.size(), user.id(), deviceId);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON format: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public int deleteGeoFencing(User user, UUID deviceId) {
        var sql = "UPDATE device d SET geo_length = 0, geo_fencing = '[]'::JSONB FROM user_role ur "
            + "WHERE d.id = ur.device_id AND ur.user_id = ? AND platform_device_id = ?";

        return jdbc.update(sql, user.id(), deviceId);
    }

    @Override
    public int delete(User user, UUID platformID) {
        var sql = "DELETE FROM device d USING user_role ur WHERE d.id = ur.device_id AND platform_device_id = ? "
            + "AND ur.user_id = ?";
        return jdbc.update(sql, platformID, user.id());
    }

    private Device parseDevice(ResultSet rs) throws SQLException {
        var lat = rs.getObject("last_lat", Double.class);
        var lon = rs.getObject("last_lon", Double.class);
        return new Device().id(rs.getInt("id"))
            .name(rs.getString("name"))
            .imei(rs.getString("imei"))
            .battery(rs.getInt("battery"))
            .platformId(rs.getObject("platform_device_id", UUID.class))
            .latitude(lat == null ? 0 : lat)
            .longitude(lon == null ? 0 : lon)
            .geoFencing(rs.getString("geo_fencing"))
            .uptime(rs.getTimestamp("update_instant").toLocalDateTime());
    }
}
