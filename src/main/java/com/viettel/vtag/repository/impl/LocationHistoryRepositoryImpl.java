package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.LocationHistoryRequest;
import com.viettel.vtag.repository.interfaces.LocationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LocationHistoryRepositoryImpl implements LocationHistoryRepository {

    private final JdbcTemplate jdbc;

    @Override
    public int save(LocationHistory location) {
        var sql = "INSERT INTO location_history(device_id, latitude, longitude, trigger_instant) VALUES (?, ?, ?, ?)";
        return jdbc.update(sql, location.deviceId(), location.latitude(), location.longitude(), location.time());
    }

    @Override
    public List<LocationHistory> fetch(User user, LocationHistoryRequest request) {
        var sql = "SELECT lh.device_id, latitude, longitude, trigger_instant FROM location_history lh "
            + "JOIN user_role ur ON lh.device_id = ur.device_id WHERE user_id = ? AND lh.device_id = ? "
            + "AND trigger_instant > ? AND trigger_instant < ?";
        return jdbc.query(sql, new Object[] {user.id(), request.deviceId(), request.from(), request.to()},
            (rs, num) -> new LocationHistory().deviceId(rs.getObject("device_id", UUID.class))
                .latitude(rs.getDouble("latitude"))
                .longitude(rs.getDouble("longitude"))
                .time(rs.getTimestamp("trigger_instant").toLocalDateTime()));
    }
}
