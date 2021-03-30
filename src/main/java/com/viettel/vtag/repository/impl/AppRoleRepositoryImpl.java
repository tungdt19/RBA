package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.AppRole;
import com.viettel.vtag.repository.interfaces.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AppRoleRepositoryImpl implements RoleRepository {

    private final JdbcTemplate jdbc;

    @Override
    public List<AppRole> getRoles(int userId) {
        var sql = "SELECT user_id, device_id, role_id FROM user_role WHERE user_id = ?";

        return jdbc.query(sql, new Object[] {userId},
            (rs, rowNum) -> new AppRole().userId(userId).deviceId(rs.getInt("device_id")).roleId(rs.getInt("role_id")));
    }

    @Override
    public List<AppRole> getRoles(String token) {
        var sql = "SELECT user_id, device_id, role_id FROM user_role ur JOIN \"user\" u ON u.id = ur.user_id "
            + "JOIN app_role ar ON ar.id = ur.role_id WHERE username = ? OR email = ? OR phone_no = ?";

        return jdbc.query(sql, new Object[] {token}, (rs, rowNum) -> new AppRole().userId(rs.getInt("user_id"))
            .deviceId(rs.getInt("device_id"))
            .roleId(rs.getInt("role_id")));
    }
}
