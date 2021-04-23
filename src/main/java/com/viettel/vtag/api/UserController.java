package com.viettel.vtag.api;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.model.response.ObjectResponse;
import com.viettel.vtag.service.impl.UserServiceImpl;
import com.viettel.vtag.service.interfaces.OtpService;
import com.viettel.vtag.service.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static com.viettel.vtag.model.response.ObjectResponse.of;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final OtpService otpService;
    private final UserService userService;

    @PostMapping("/otp/register")
    public Mono<ResponseEntity<ObjectResponse>> registerOtp(@RequestBody OtpRequest request, Locale locale) {
        return otpService.sendRegisterOtp(request, locale)
            .map(otp -> ok(of(0, "Created OTP successfully!", otp)))
            .defaultIfEmpty(status(CONFLICT).body(of(1, "User's already existed!")))
            .doOnError(e -> log.error("Couldn't send OTP: {}", e.getMessage()))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't create OTP")));
    }

    /** {@link UserServiceImpl#register} */
    @PostMapping("/register")
    public Mono<ResponseEntity<ObjectResponse>> register(@RequestBody RegisterRequest request) {
        return userService.register(request)
            .map(registered -> {
                switch (registered) {
                    case 1: return ok(of(0, "Created user successfully!"));
                    case -1: return status(CONFLICT).body(of(1, "User's already existed!"));
                    default: return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't create user!"));
                }
            })
            .defaultIfEmpty(status(CONFLICT).body(of(1, "Couldn't create platform user!")))
            .onErrorReturn(badRequest().body(of(1, "Couldn't create user!")));
    }

    @PostMapping("/otp/reset")
    public Mono<ResponseEntity<ObjectResponse>> resetOtp(@RequestBody OtpRequest request, Locale locale) {
        return otpService.sendResetOtp(request, locale)
            .map(otp -> ok(of(0, "Created OTP successfully!", otp)))
            .defaultIfEmpty(status(NOT_FOUND).body(of(1, "User does not exist!")))
            .doOnError(e -> log.error("Couldn't send OTP: {}", e.getMessage()))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't create OTP")));
    }

    /**
     * {@link UserServiceImpl#createToken}
     */
    @PostMapping("/token")
    public Mono<ResponseEntity<ObjectResponse>> getToken(@RequestBody TokenRequest request) {
        return userService.createToken(request)
            .map(token -> ok(of(0, "Get token successfully!", token)))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Invalid phone number or password!")));
    }

    /** {@link UserServiceImpl#updateNotificationToken(User, FcmTokenUpdateRequest)} */
    @PostMapping("/notification")
    public Mono<ResponseEntity<ObjectResponse>> updateFcmToken(
        @RequestBody FcmTokenUpdateRequest detail, ServerHttpRequest request
    ) {
        //@formatter:off
        return userService.checkToken(request)
            .flatMap(user -> userService.updateNotificationToken(user, detail))
            .map(updated -> updated > 0
                ? ok(of(0, "Okie dokie!"))
                : ok(of(1, "Couldn't update FCM token!")))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Invalid username or password!")));
        //@formatter:on
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<ObjectResponse>> getUserInfo(ServerHttpRequest request) {
        return userService.checkToken(request)
            .map(user -> status(OK).body(of(0, "Okie dokie!", user)))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Your user token is invalid!")));
    }

    @PostMapping("/password")
    public Mono<ResponseEntity<ObjectResponse>> changePassword(
        @RequestBody ChangePasswordRequest detail, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .flatMap(user -> userService.changePassword(user, detail))
            .map(changed -> changed > 0
                ? ok(of(0, "Changed password successfully!"))
                : badRequest().body(of(1, "Couldn't changed password!")))
            .doOnError(e -> log.error("Couldn't change password {}: {}", detail, e.getMessage()))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Get lost, trespasser!")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't change password!")));
    }

    /** {@see UserServiceImpl#resetPassword} */
    @PostMapping("/password/reset")
    public Mono<ResponseEntity<ObjectResponse>> resetPassword(@RequestBody ResetPasswordRequest detail) {
        return userService.resetPassword(detail)
            .map(reset -> reset > 0
                ? ok(of(0, "Changed password successfully!"))
                : badRequest().body(of(1, "Couldn't reset password!")))
            .doOnError(e -> log.error("Couldn't reset password {}: {}", detail, e.getMessage()))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't reset password!")));
    }

    @DeleteMapping("/token")
    public ResponseEntity<ObjectResponse> signout(ServerHttpRequest request) {
        return badRequest().body(of(1, "Couldn't delete token!"));
    }

    @DeleteMapping
    public Mono<ResponseEntity<ObjectResponse>> deleteUser(ServerHttpRequest request) {
        //@formatter:off
        return userService.checkToken(request)
            .flatMap(userService::delete)
            .map(deleted -> deleted > 0
                ? ok(of(0, "Delete user successfully!"))
                : ok(of(1, "Couldn't delete user!")))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Get lost, trespasser!")))
            .doOnError(e -> log.error("Couldn't delete user!", e))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't delete user!")));
        //@formatter:on
    }
}
