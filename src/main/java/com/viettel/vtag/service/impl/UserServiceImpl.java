package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.Identity;
import com.viettel.vtag.model.entity.Token;
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
    public Mono<Integer> register(RegisterRequest request) {
        var phone = PhoneUtils.standardize(request.phone());
        return iotPlatformService.post("/api/groups", Map.of("name", phone))
            .doOnNext(response -> log.info("register user {}: {}", phone, response.statusCode()))
            .filter(response -> response.statusCode().is2xxSuccessful())
            .flatMap(entity -> entity.bodyToMono(Identity.class))
            .map(identity -> new User().phone(phone)
                .password(request.password())
                .platformId(UUID.fromString(identity.id())))
            .map(userRepository::register)
            .doOnError(e -> log.error("Couldn't register user with phone {}", phone))
            .onErrorReturn(DuplicateKeyException.class::isInstance, -1);
    }

    @Override
    public Token createToken(TokenRequest request) {
        try {
            var phone = PhoneUtils.standardize(request.username());
            var user = userRepository.findByPhone(phone);
            if (user == null || !bCrypt.matches(request.password(), user.encryptedPassword())) return null;
            log.info("User({}, {}, {})", user.id(), user.phone(), user.platformId());

            var token = Token.generate();
            var updated = userRepository.saveToken(token, user.id());
            return updated > 0 ? token : null;
        } catch (Exception e) {
            log.error("cannot create token {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Mono<Integer> changePassword(User user, ChangePasswordRequest request) {
        return Mono.just(bCrypt.matches(request.oldPassword(), user.encryptedPassword()))
            .filter(matched -> matched)
            .map(matched -> userRepository.updatePassword(user.phone(), request.newPassword()));
    }

    @Override
    public int resetPassword(ResetPasswordRequest request) {
        return userRepository.updatePassword(request.phone(), request.password());
    }

    @Override
    public Mono<Integer> delete(User user) {
        return Mono.just(userRepository.delete(user)).filter(deleted -> deleted > 0);
    }

    @Override
    public User checkUserToken(String token) {
        return userRepository.findByToken(token);
    }

    @Override
    public int updateNotificationToken(User user, FcmTokenUpdateRequest request) {
        //TODO implement this
        return userRepository.updateNotificationToken(user, request);
    }
}
