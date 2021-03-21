package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.AddViewerRequest;
import com.viettel.vtag.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final JdbcTemplate jdbc;

    @Override
    public int add(User user, AddViewerRequest request) {
        var sql = "INSERT INTO user_role(user_id, device_id, role_id) SELECT ?, id, ? FROM device d WHERE imei = ?";
        return jdbc.update(sql, user.id(), request.imei(), "ROLE_VIEWER");
    }

    @Override
    public String getInfo() {
        //TODO: implement this
        return null;
    }

    @Override
    public String getList() {
        //TODO: implement this
        return null;
    }
}
