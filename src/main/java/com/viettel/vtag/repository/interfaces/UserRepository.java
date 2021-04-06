package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.FcmTokenUpdateRequest;
import com.viettel.vtag.model.request.ResetPasswordRequest;

import java.util.List;

public interface UserRepository {

    User find(String token);

    User findByPhone(String phone);

    User findByEmail(String email);

    int register(User user);

    int update(User user);

    int updatePassword(String phone, String newPassword);

    int delete(User user);

    int saveToken(Object token, int userId);

    int updateNotificationToken(User user, FcmTokenUpdateRequest request);

    User findByToken(String token);

    List<String> fetchAllViewers(String deviceId);
}
