package com.viettel.vtag.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.repository.cache.DeviceCache;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DeviceRepositoryImpl implements DeviceRepository, RowMapper<Device> {

    private final ObjectMapper mapper = new ObjectMapper();

    private final DeviceCache cache;
    private final JdbcTemplate jdbc;

    @Override
    public Device get(UUID deviceId) {
        var device = cache.get(deviceId);
        if (device != null) return device;

        try {
            var sql = "SELECT d.* FROM device d WHERE platform_device_id = ?";
            return jdbc.queryForObject(sql, this, deviceId);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
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
    public List<Device> getAllDevices() {
        var sql = "SELECT d.* FROM device d";
        return jdbc.query(sql, this);
    }

    @Override
    public List<UUID> fetchAllDevices() {
        var sql = "SELECT platform_device_id FROM device WHERE platform_device_id IS NOT NULL";
        return jdbc.query(sql, (rs, rowNum) -> rs.getObject("platform_device_id", UUID.class));
    }

    @Override
    public List<Device> getUserDevice(User user) {
        var sql = "SELECT d.* FROM device d INNER JOIN user_role ur ON d.id = ur.device_id WHERE user_id = ?";
        return jdbc.query(sql, this, user.id());
    }

    @Override
    public Device getUserDevice(User user, UUID deviceId) {
        var sql = "SELECT d.* FROM device d INNER JOIN user_role ur ON d.id = ur.device_id WHERE user_id = ? "
            + "AND platform_device_id = ?";
        return jdbc.queryForObject(sql, this, user.id(), deviceId);
    }

    @Override
    public String getGeoFencing(User user, UUID deviceId) {
        try {
            var sql = "SELECT d.* FROM device d INNER JOIN user_role ur ON id = ur.device_id WHERE user_id = ? "
                + "AND platform_device_id = ?";
            return jdbc.queryForObject(sql, this, user.id(), deviceId).geoFencing();
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    public int updateGeoFencing(User user, UUID deviceId, List<Fence> fences) {
        try {
            var sql = "UPDATE device d SET geo_fencing = ?::JSONB, geo_length = ? FROM user_role ur "
                + "WHERE ur.device_id = d.id AND ur.user_id = ? AND platform_device_id = ?";
            cache.get(deviceId).fences(fences);
            return jdbc.update(sql, mapper.writeValueAsString(fences), fences.size(), user.id(), deviceId);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON format: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public int deleteGeoFencing(User user, UUID deviceId) {
        var sql = "UPDATE device d SET geo_length = 0, geo_fencing = '[]'::JSONB FROM user_role ur "
            + "WHERE d.id = ur.device_id AND ur.user_id = ? AND platform_device_id = ?";
        cache.get(deviceId).fences(List.of());
        return jdbc.update(sql, user.id(), deviceId);
    }

    @Override
    public int setUserDevice(User user, PairDeviceRequest request) {
        var sql = "INSERT INTO user_role (device_id, user_id, role_id) SELECT d.id, ?, ? FROM device d "
            + "WHERE platform_device_id = ?";
        return jdbc.update(sql, user.id(), 1, request.platformId());
    }

    @Override
    public List<LocationHistory> fetchHistory(User user, LocationHistoryRequest request) {
        var sql = "SELECT latitude, longitude, trigger_instant, lh.accuracy FROM location_history lh JOIN user_role ur "
            + "ON lh.device_id = ur.device_id JOIN device d ON d.id = ur.device_id WHERE ur.user_id = ? "
            + "AND trigger_instant > ? AND trigger_instant < ? AND platform_device_id = ? ORDER BY trigger_instant";
        return jdbc.query(sql, new Object[] {user.id(), request.from(), request.to(), request.deviceId()},
            (rs, num) -> new LocationHistory().latitude(rs.getDouble("latitude"))
                .longitude(rs.getDouble("longitude"))
                .accuracy(rs.getInt("accuracy"))
                .time(rs.getTimestamp("trigger_instant").toLocalDateTime()));
    }

    @Override
    public int delete(User user, UUID platformId) {
        var sql = "DELETE FROM device d WHERE platform_device_id = ?";
        cache.remove(platformId);
        return jdbc.update(sql, platformId);
    }

    @Override
    public Device mapRow(ResultSet rs, int i) throws SQLException {
        var lat = rs.getObject("last_lat", Double.class);
        var lon = rs.getObject("last_lon", Double.class);
        var uuid = rs.getObject("platform_device_id", UUID.class);
        var geoFencing = rs.getString("geo_fencing");
        var device = new Device().id(rs.getInt("id"))
            .name(rs.getString("name"))
            .imei(rs.getString("imei"))
            .battery(rs.getInt("battery"))
            .status(rs.getInt("status"))
            .platformId(uuid)
            .latitude(lat == null ? 0 : lat)
            .longitude(lon == null ? 0 : lon)
            .accuracy(rs.getInt("accuracy"))
            .uptime(rs.getTimestamp("update_instant").toLocalDateTime())
            .geoFencing(geoFencing);
        cache.put(uuid, device.parseGeoFencing(geoFencing));
        return device;
    }
}
