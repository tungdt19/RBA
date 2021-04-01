package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.repository.interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final BCryptPasswordEncoder bCrypt;
    private final JdbcTemplate jdbc;

    @Override
    public User find(String token) {
        try {
            var sql = "SELECT id, username, password, first_name, last_name, email, phone_no, avatar FROM \"user\" "
                + "WHERE username = ? OR email = ? OR phone_no = ?";
            return jdbc.queryForObject(sql, new Object[] {token, token, token}, (rs, num) -> mapUser(rs));
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public User findByPhone(String phone) {
        try {
            var sql = "SELECT id, username, password, first_name, last_name, email, phone_no, avatar FROM \"user\" "
                + "WHERE phone_no = ?";
            return jdbc.queryForObject(sql, new Object[] {phone}, (rs, num) -> mapUser(rs));
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public User findByEmail(String email) {
        try {
            var sql = "SELECT id, username, password, first_name, last_name, email, phone_no, avatar FROM \"user\" "
                + "WHERE email = ?";
            return jdbc.queryForObject(sql, new Object[] {email}, (rs, num) -> mapUser(rs));
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User().id(rs.getInt("id"))
            .username(rs.getString("username"))
            .encryptedPassword(rs.getString("password"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .email(rs.getString("email"))
            .phoneNo(rs.getString("phone_no"))
            .avatar(rs.getString("avatar"));
    }

    @Override
    public int register(User user) {
        var sql = "INSERT INTO \"user\" (username, password, first_name, last_name, email, phone_no, avatar) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        var password = bCrypt.encode(user.password());
        return jdbc.update(sql, user.username(), password, user.firstName(), user.lastName(), user.email(),
            user.phoneNo(), user.avatar());
    }

    @Override
    public int update(User user) {
        var sql = "UPDATE \"user\" SET password = ?, first_name = ?, last_name = ?, email = ?, phone_no = ?, avatar = ?"
            + " WHERE username = ?";
        var password = bCrypt.encode(user.password());
        return jdbc.update(sql, user.username(), password, user.firstName(), user.lastName(), user.email(),
            user.phoneNo(), user.avatar());
    }

    @Override
    public int updatePassword(User user, String newPassword) {
        var sql = "UPDATE \"user\" SET password = ? WHERE username = ?";
        return jdbc.update(sql, bCrypt.encode(newPassword), user.username());
    }

    @Override
    public int delete(User user) {
        var sql = "DELETE FROM \"user\"  WHERE username = ?";
        return jdbc.update(sql, user.username());
    }

    @Override
    public int saveToken(Object token, int userId) {
        var sql = "INSERT INTO token (token, user_id) VALUES (?, ?)";
        return jdbc.update(sql, token, userId);
    }

    @Override
    public User findByToken(String token) {
        var sql = "SELECT token, user_id, expired_instant, id, username, password, first_name, last_name, email, "
            + "phone_no FROM token t LEFT JOIN \"user\" u ON t.user_id = u.id WHERE (expired_instant IS NULL "
            + "OR expired_instant > CURRENT_TIMESTAMP) AND t.token = ?";
        return jdbc.queryForObject(sql,
            (rs, i) -> new User().username(rs.getString("username")).encryptedPassword(rs.getString("password")),
            token);
    }
}
