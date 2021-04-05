package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.repository.interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final PasswordEncoder bCrypt;
    private final JdbcTemplate jdbc;

    @Override
    public User find(String token) {
        try {
            var sql = "SELECT id, username, password, first_name, last_name, email, phone_no, avatar FROM end_user "
                + "WHERE username = ? OR email = ? OR phone_no = ?";
            return jdbc.queryForObject(sql, new Object[] {token, token, token}, (rs, num) -> mapUser(rs));
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public User findByPhone(String phone) {
        try {
            var sql = "SELECT id, username, password, first_name, last_name, email, phone_no, avatar FROM end_user "
                + "WHERE phone_no = ?";
            return jdbc.queryForObject(sql, new Object[] {phone}, (rs, num) -> mapUser(rs));
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public User findByEmail(String email) {
        try {
            var sql = "SELECT id, username, password, first_name, last_name, email, phone_no, avatar FROM end_user "
                + "WHERE email = ?";
            return jdbc.queryForObject(sql, new Object[] {email}, (rs, num) -> mapUser(rs));
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public int register(User user) {
        var sql = "INSERT INTO end_user (username, password, first_name, last_name, email, phone_no, avatar) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        var password = bCrypt.encode(user.password());
        return jdbc.update(sql, user.username(), password, user.firstName(), user.lastName(), user.email(),
            user.phoneNo(), user.avatar());
    }

    @Override
    public int update(User user) {
        var sql = "UPDATE end_user SET password = ?, first_name = ?, last_name = ?, email = ?, phone_no = ?, avatar = ?"
            + " WHERE username = ?";
        var password = bCrypt.encode(user.password());
        return jdbc.update(sql, user.username(), password, user.firstName(), user.lastName(), user.email(),
            user.phoneNo(), user.avatar());
    }

    @Override
    public int updatePassword(User user, String newPassword) {
        var sql = "UPDATE end_user SET password = ? WHERE phone_no = ?";
        return jdbc.update(sql, bCrypt.encode(newPassword), user.phoneNo());
    }

    @Override
    public int delete(User user) {
        var sql = "DELETE FROM end_user WHERE username = ?";
        return jdbc.update(sql, user.username());
    }

    @Override
    public int saveToken(Object token, int userId) {
        var sql = "INSERT INTO token (token, user_id) VALUES (?, ?)";
        return jdbc.update(sql, token, userId);
    }

    @Override
    public int updateNotificationToken(String token, int userId) {
        return 0;
    }

    @Override
    public User findByToken(String token) {
        var sql = "SELECT token, user_id, expired_instant, id, username, password, first_name, last_name, email, "
            + "phone_no, avatar, fcm_token FROM token t LEFT JOIN end_user u ON t.user_id = u.id "
            + "WHERE (expired_instant IS NULL OR expired_instant > CURRENT_TIMESTAMP) AND t.token = ?";
        return jdbc.queryForObject(sql, new Object[] {UUID.fromString(token)}, (rs, i) -> mapUser(rs));
    }

    @Override
    public List<String> fetchAllViewers(String deviceId) {
        var uuid = UUID.fromString(deviceId);
        var sql = "SELECT fcm_token FROM user_role ur JOIN end_user u ON u.id = ur.user_id "
            + "JOIN device d ON d.id = ur.device_id WHERE platform_device_id = ?";
        return jdbc.query(sql, new Object[] {uuid}, (rs, rowNum) -> rs.getString("fcm_token"));
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User().id(rs.getInt("id"))
            .username(rs.getString("username"))
            .encryptedPassword(rs.getString("password"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .email(rs.getString("email"))
            .phoneNo(rs.getString("phone_no"))
            .avatar(rs.getString("avatar"))
            .fcmToken(rs.getString("fcm_token"));
    }
}
