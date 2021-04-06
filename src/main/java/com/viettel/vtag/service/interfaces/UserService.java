package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.utils.TokenUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<Integer> save(User user);

    String createToken(TokenRequest request);

    int changePassword(User user, ChangePasswordRequest password);

    int resetPassword(ResetPasswordRequest request);

    int delete(User user);

    default User checkToken(ServerHttpRequest request) {
        return checkToken(TokenUtils.getToken(request));
    }

    User checkToken(String token);

    int updateNotificationToken(User user, FcmTokenUpdateRequest request);
}
