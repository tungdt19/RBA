package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.Identity;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.repository.impl.UserRepositoryImpl;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import com.viettel.vtag.service.interfaces.UserService;
import com.viettel.vtag.utils.PhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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

    /** {@link UserRepositoryImpl#register(User)} */
    @Override
    public Mono<Integer> save(User user) {
        var phone = PhoneUtils.standardize(user.phoneNo());
        user.phoneNo(phone);
        return iotPlatformService.post("/api/groups", Map.of("name", phone)).flatMap(entity -> {
            var statusCode = entity.statusCode();
            log.info("register user {}: {}", phone, statusCode);

            var result = entity.bodyToMono(Identity.class).flatMap(identity -> {
                log.info("/api/groups/{}: {} -> {}", phone, statusCode, identity);
                user.platformId(UUID.fromString(identity.id()));
                return Mono.just(userRepository.register(user));
            });
            if (statusCode.is2xxSuccessful()) {
                return result;
            }
            log.error("Couldn't register user with phone {}", user.phoneNo());
            return Mono.just(-1);
        }).onErrorReturn(DuplicateKeyException.class::isInstance, -1);
    }

    @Override
    public String createToken(TokenRequest request) {
        try {
            var phone = PhoneUtils.standardize(request.username());
            var user = userRepository.findByPhone(phone);
            log.info("create token {}", user);
            if (user == null || !bCrypt.matches(request.password(), user.encryptedPassword())) return null;

            var token = UUID.randomUUID();
            var updated = userRepository.saveToken(token, user.id());
            return updated > 0 ? token.toString() : null;
        } catch (Exception e) {
            log.error("cannot create token {}", e.getMessage());
            return null;
        }
    }

    @Override
    public int changePassword(User user, ChangePasswordRequest request) {
        if (!bCrypt.matches(request.oldPassword(), user.encryptedPassword())) {
            log.info("Password does not match for user {}", user.phoneNo());
            return 0;
        }

        return userRepository.updatePassword(user.phoneNo(), request.newPassword());
    }

    @Override
    public int resetPassword(ResetPasswordRequest request) {
        return userRepository.updatePassword(request.phone(), request.password());
    }

    @Override
    public int delete(User user) {
        return 0;
    }

    @Override
    public User checkToken(String token) {
        return userRepository.findByToken(token);
    }

    @Override
    public int updateNotificationToken(User user, FcmTokenUpdateRequest request) {
        //TODO implement this
        return userRepository.updateNotificationToken(user, request);
    }
}
