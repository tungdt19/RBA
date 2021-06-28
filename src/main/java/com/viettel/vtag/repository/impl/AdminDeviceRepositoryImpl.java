package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.repository.interfaces.AdminDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AdminDeviceRepositoryImpl implements AdminDeviceRepository, RowMapper<Device> {

    private final JdbcTemplate jdbc;

    @Override
    public List<Device> getAllDevices() {
        var sql = "SELECT d.* FROM device d";
        return jdbc.query(sql, this);
    }

    @Override
    public List<String> getAllViewerTokens(UUID platformId) {
        var sql = "SELECT fcm_token FROM end_user u JOIN user_role ur ON u.id = ur.user_id JOIN device d "
            + "ON d.id = ur.device_id WHERE fcm_token IS NOT NULL AND d.platform_device_id = ?";
        return jdbc.query(sql, new Object[]{platformId}, (rs, i) -> rs.getString("fcm_token"));
    }

    @Override
    public List<LocationHistory> getDeviceHistory(UUID device) {
        return null;
    }

    @Override
    public Device mapRow(ResultSet rs, int i) throws SQLException {
        var lat = rs.getObject("last_lat", Double.class);
        var lon = rs.getObject("last_lon", Double.class);
        var uuid = rs.getObject("platform_device_id", UUID.class);
        var geoFencing = rs.getString("geo_fencing");
        return new Device().id(rs.getInt("id"))
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
    }
}
