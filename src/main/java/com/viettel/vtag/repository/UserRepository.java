package com.viettel.vtag.repository;

import com.viettel.vtag.model.entity.User;

public interface UserRepository {

    User find(String token);

    int save(User user);

    int update(User user);

    int updatePassword(User user, String newPassword);

    int delete(User user);

    int saveToken(Object token, int userId);

    User findByToken(String token);
}
