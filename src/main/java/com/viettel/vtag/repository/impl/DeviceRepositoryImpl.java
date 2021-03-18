package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DeviceRepositoryImpl implements DeviceRepository {

    private final JdbcTemplate jdbc;

    @Override
    public Device get(int id) {
        var sql = "SELECT id, name, imei FROM device WHERE id = ?";
        return jdbc.queryForObject(sql, new Object[] {id}, (rs, num) -> parseDevice(rs));
    }

    private Device parseDevice(ResultSet rs) throws SQLException {
        return new Device().id(rs.getInt("id")).name(rs.getString("name")).imei(rs.getString("imei"));
    }

    @Override
    public Device find(String imei) {
        var sql = "SELECT id, name, imei FROM device WHERE imei = ?";
        return jdbc.queryForObject(sql, new Object[] {imei}, (rs, num) -> parseDevice(rs));
    }

    @Override
    public int save(Device device) {
        var sql = "INSERT INTO device(name, imei) VALUES (?, ?)";
        return jdbc.update(sql, device.name(), device.imei());
    }

    @Override
    public int update(Device device) {
        var sql = "UPDATE device SET name = ?, imei = ? WHERE id = ?";
        return jdbc.update(sql, device.name(), device.imei(), device.id());
    }

    @Override
    public int delete(Device device) {
        var sql = "DELETE FROM device WHERE id = ?";
        return jdbc.update(sql, device.id());
    }
}
