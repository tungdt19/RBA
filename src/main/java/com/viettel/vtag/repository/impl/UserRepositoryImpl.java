package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.Token;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.FcmTokenUpdateRequest;
import com.viettel.vtag.repository.interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository, RowMapper<User> {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder bCrypt;

    @Override
    public User find(String token) {
        try {
            var sql = "SELECT id, username, password, first_name, last_name, email, phone_no, avatar FROM end_user "
                + "WHERE username = ? OR email = ? OR phone_no = ?";
            return jdbc.queryForObject(sql, this, token, token, token);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public User findByPhone(String phone) {
        try {
            var sql = "SELECT id, username, password, first_name, last_name, email, phone_no, avatar, fcm_token, "
                + "platform_group_id FROM end_user WHERE phone_no = ?";
            return jdbc.queryForObject(sql, this, phone);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public User findByEmail(String email) {
        try {
            var sql = "SELECT id, username, password, first_name, last_name, email, phone_no, avatar FROM end_user "
                + "WHERE email = ?";
            return jdbc.queryForObject(sql, this, email);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public int register(User user) {
        var sql = "INSERT INTO end_user (username, password, first_name, last_name, email, phone_no, avatar, "
            + "platform_group_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        var password = bCrypt.encode(user.password());
        return jdbc.update(sql, user.username(), password, user.firstName(), user.lastName(), user.email(),
            user.phone(), user.avatar(), user.platformId());
    }

    @Override
    public int update(User user) {
        var sql = "UPDATE end_user SET password = ?, first_name = ?, last_name = ?, email = ?, phone_no = ?, avatar = ?"
            + " WHERE username = ?";
        var password = bCrypt.encode(user.password());
        return jdbc.update(sql, user.username(), password, user.firstName(), user.lastName(), user.email(),
            user.phone(), user.avatar());
    }

    @Override
    public int updatePassword(String phone, String newPassword) {
        var sql = "UPDATE end_user SET password = ? WHERE phone_no = ?";
        return jdbc.update(sql, bCrypt.encode(newPassword), phone);
    }

    @Override
    public int delete(User user) {
        var sql = "DELETE FROM end_user WHERE platform_group_id = ?";
        return jdbc.update(sql, user.platformId());
    }

    @Override
    public int saveToken(Token token, int userId) {
        var sql = "INSERT INTO token (user_id, token, expired_instant) VALUES (?, ?, ?)";
        return jdbc.update(sql, userId, token.uuid(), token.expire());
    }

    @Override
    public int updateNotificationToken(User user, FcmTokenUpdateRequest request) {
        var sql = "UPDATE end_user SET fcm_token = ? WHERE id = ?";
        return jdbc.update(sql, request.fcmToken(), user.id());
    }

    @Override
    public User findByToken(UUID token) {
        if (token == null) return null;

        try {
            var sql = "SELECT token, user_id, expired_instant, id, username, password, first_name, last_name, email, "
                + "phone_no, avatar, fcm_token, platform_group_id FROM token LEFT JOIN end_user u ON user_id = u.id "
                + "WHERE token = ?"; // AND (expired_instant IS NULL OR expired_instant > CURRENT_TIMESTAMP)
            return jdbc.queryForObject(sql, this, token);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public User mapRow(ResultSet rs, int i) throws SQLException {
        return new User().id(rs.getInt("id"))
            .username(rs.getString("username"))
            .encryptedPassword(rs.getString("password"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .email(rs.getString("email"))
            .phone(rs.getString("phone_no"))
            .avatar(rs.getString("avatar"))
            .fcmToken(rs.getString("fcm_token"))
            .platformId(rs.getObject("platform_group_id", UUID.class));
    }
}
