package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.User;

import java.util.List;

public interface UserRepository {

    User find(String token);

    User findByPhone(String phone);

    User findByEmail(String email);

    int register(User user);

    int update(User user);

    int updatePassword(User user, String newPassword);

    int delete(User user);

    int saveToken(Object token, int userId);

    User findByToken(String token);

    List<String> fetchAllViewers(String deviceId);
}
