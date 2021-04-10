package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.utils.TokenUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<Integer> register(RegisterRequest request);

    String createToken(TokenRequest request);

    Mono<Integer> changePassword(User user, ChangePasswordRequest password);

    int resetPassword(ResetPasswordRequest request);

    Mono<Integer> delete(User user);

    default User checkToken(ServerHttpRequest request) {
        return checkToken(TokenUtils.getToken(request));
    }

    User checkToken(String token);

    int updateNotificationToken(User user, FcmTokenUpdateRequest request);
}
