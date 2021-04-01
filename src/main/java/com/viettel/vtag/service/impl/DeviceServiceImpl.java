package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.AddViewerRequest;
import com.viettel.vtag.model.request.RemoveViewerRequest;
import com.viettel.vtag.service.interfaces.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final JdbcTemplate jdbc;

    @Override
    public int addViewer(User user, AddViewerRequest request) {
        var sql = "INSERT INTO user_role(user_id, device_id, role_id) SELECT ?, id, ? FROM device d WHERE imei = ?";
        return jdbc.update(sql, user.id(), request.imei(), "ROLE_VIEWER");
    }

    @Override
    public String getInfo() {
        //TODO: implement this
        return null;
    }

    @Override
    public List<Device> getList(User user) {
        var sql = "SELECT id, name, imei FROM device JOIN user_role ur ON device.id = ur.device_id WHERE user_id = ?";
        return jdbc.query(sql, new Object[] {user.id()},
            (rs, rowNum) -> new Device().id(rs.getInt("id")).name(rs.getString("name")).imei(rs.getString("imei")));
    }

    @Override
    public int remove(User user, RemoveViewerRequest detail) {
        var sql = "DELETE FROM user_role v USING user_role o "
            + "WHERE o.role_id = 1 AND o.device_id = v.device_id AND o.user_id = ? AND v.user_id = ?";
        return jdbc.update(sql, user.id(), detail.viewerId());
    }
}
