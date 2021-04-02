package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.ChangePasswordRequest;
import com.viettel.vtag.model.request.FcmTokenUpdateRequest;
import com.viettel.vtag.model.request.TokenRequest;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.UserService;
import com.viettel.vtag.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCrypt;

    @Override
    public int save(User user) {
        return userRepository.register(user);
    }

    @Override
    public String createToken(TokenRequest request) {
        var user = userRepository.find(request.username());
        log.debug("user {}", user);
        if (bCrypt.matches(request.password(), user.encryptedPassword())) {
            var token = UUID.randomUUID();
            var updated = userRepository.saveToken(token, user.id());
            return updated > 0 ? token.toString() : null;
        } else {
            return null;
        }
    }

    @Override
    public User checkToken(String token) {
        return userRepository.findByToken(token);
    }

    @Override
    public User checkToken(ServerHttpRequest request) {
        return checkToken(TokenUtils.getToken(request));
    }

    @Override
    public int updateNotificationToken(FcmTokenUpdateRequest request) {

        return 0;
    }

    @Override
    public int changePassword(User user, ChangePasswordRequest request) {
        if (!bCrypt.matches(request.oldPassword(), user.encryptedPassword())) return 0;

        return userRepository.updatePassword(user, request.newPassword());
    }

    @Override
    public int delete(User user) {
        return 0;
    }
}
