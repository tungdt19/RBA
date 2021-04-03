package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.Identity;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.ChangePasswordRequest;
import com.viettel.vtag.model.request.FcmTokenUpdateRequest;
import com.viettel.vtag.model.request.TokenRequest;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import com.viettel.vtag.service.interfaces.UserService;
import com.viettel.vtag.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final IotPlatformService iotPlatformService;
    private final PasswordEncoder bCrypt;

    @Override
    public Mono<Integer> save(User user) {
        return iotPlatformService.post("/api/groups", Map.of("name", user.phoneNo()), Identity.class)
            .flatMap(entity -> Mono.justOrEmpty(entity.getBody()))
            .flatMap(identity -> Mono.just(userRepository.register(user.platformId(identity.id()))));
    }

    @Override
    public String createToken(TokenRequest request) {
        var user = userRepository.findByPhone(request.username());
        log.info("user {}", user);
        if (user == null) return null;

        if (bCrypt.matches(request.password(), user.encryptedPassword())) {
            var token = UUID.randomUUID();
            var updated = userRepository.saveToken(token, user.id());
            return updated > 0 ? token.toString() : null;
        }

        return null;
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
        var sql = "up";
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
