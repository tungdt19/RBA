package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.repository.interfaces.AdminDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminDeviceRepositoryImpl implements AdminDeviceRepository {

    private final JdbcTemplate jdbc;

    @Override
    public List<Device> getAllDevices() {
        return null;
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
}
