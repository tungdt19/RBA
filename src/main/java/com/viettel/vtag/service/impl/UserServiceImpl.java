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
import com.viettel.vtag.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

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
        return Mono.justOrEmpty(request.password())
            .flatMap(s -> iotPlatformService.post("/api/groups", Map.of("name", phone)))
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
    public Mono<Token> createToken(TokenRequest request) {
        return Mono.justOrEmpty(PhoneUtils.standardize(request.username()))
            .map(userRepository::findByPhone)
            .filter(user -> bCrypt.matches(request.password(), user.encryptedPassword()))
            .zipWith(Mono.justOrEmpty(Token.generate()))
            .filter(tuple -> userRepository.saveToken(tuple.getT2(), tuple.getT1().id()) > 0)
            .doOnNext(tuple -> {
                var user = tuple.getT1();
                log.info("User({}, {}, {}) -> {}", user.id(), user.phone(), user.platformId(), tuple.getT2());
            })
            .map(Tuple2::getT2)
            .doOnError(e -> log.error("cannot create token {}", e.getMessage()));
    }

    @Override
    public Mono<Integer> changePassword(User user, ChangePasswordRequest request) {
        return Mono.just(bCrypt.matches(request.oldPassword(), user.encryptedPassword()))
            .filter(matched -> matched)
            .map(matched -> userRepository.updatePassword(user.phone(), request.newPassword()));
    }

    @Override
    public Mono<Integer> resetPassword(ResetPasswordRequest request) {
        var phone = PhoneUtils.standardize(request.phone());
        return Mono.just(userRepository.updatePassword(phone, request.password()));
    }

    @Override
    public Mono<Integer> delete(User user) {
        return Mono.just(userRepository.delete(user)).filter(deleted -> deleted > 0);
    }

    @Override
    public Mono<User> checkToken(ServerHttpRequest request) {
        //@formatter:off
        return Mono.justOrEmpty(TokenUtils.getToken(request))
            .flatMap(token -> {
                try {
                    return Mono.just(UUID.fromString(token));
                } catch (Exception e) {
                    log.error("Couldn't parse token {}: {}", token, e.getMessage());
                    return Mono.empty();
                }
            })
            .map(userRepository::findByToken);
        //@formatter:on
    }

    @Override
    public Mono<Integer> updateNotificationToken(User user, FcmTokenUpdateRequest request) {
        return Mono.just(userRepository.updateNotificationToken(user, request));
    }
}
