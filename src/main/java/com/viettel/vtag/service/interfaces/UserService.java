package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.Token;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.utils.TokenUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<Integer> register(RegisterRequest request);

    Mono<Token> createToken(TokenRequest request);

    Mono<Integer> changePassword(User user, ChangePasswordRequest password);

    Mono<Integer> resetPassword(ResetPasswordRequest request);

    Mono<Integer> delete(User user);

    default Mono<User> checkToken(ServerHttpRequest request) {
        return Mono.justOrEmpty(checkUserToken(TokenUtils.getToken(request)));
    }

    User checkUserToken(String token);

    Mono<Integer> updateNotificationToken(User user, FcmTokenUpdateRequest request);
}
