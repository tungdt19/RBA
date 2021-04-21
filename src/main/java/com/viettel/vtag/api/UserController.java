package com.viettel.vtag.api;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.model.response.ResponseBody;
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
import java.util.Map;

import static com.viettel.vtag.model.response.ResponseBody.of;
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
    public ResponseEntity<ResponseBody> registerOtp(@RequestBody OtpRequest request, Locale locale) {
        try {
            var otp = otpService.generateRegisterOtp(request);
            if (otp == null) {
                return status(CONFLICT).body(of(1, "User's already existed!"));
            }

            otpService.sendOtp(request, otp, locale);
            return ok(of(0, "Created OTP successfully!", otp));
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't create OTP", detail));
        }
    }

    /** {@link UserServiceImpl#register} */
    @PostMapping("/register")
    public Mono<ResponseEntity<ResponseBody>> register(@RequestBody RegisterRequest request) {
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
    public ResponseEntity<ResponseBody> resetOtp(@RequestBody OtpRequest request, Locale locale) {
        try {
            var otp = otpService.generateResetOtp(request);
            if (otp == null) {
                return status(NOT_FOUND).body(of(1, "User does not exist!"));
            }

            otpService.sendOtp(request, otp, locale);
            var data = Map.of("otp", otp.content(), "expire", otp.expiredInstant());
            return ok(of(0, "Created OTP successfully!", data));
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't create OTP", detail));
        }
    }

    /**
     * {@link UserServiceImpl#createToken}
     */
    @PostMapping("/token")
    public Mono<ResponseEntity<ResponseBody>> getToken(@RequestBody TokenRequest request) {
        return userService.createToken(request)
            .map(token -> ok(of(0, "Get token successfully!", token)))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Invalid phone number or password!")));
    }

    /** {@link UserServiceImpl#updateNotificationToken(User, FcmTokenUpdateRequest)} */
    @PostMapping("/notification")
    public Mono<ResponseEntity<ResponseBody>> updateFcmToken(
        @RequestBody FcmTokenUpdateRequest detail, ServerHttpRequest request
    ) {
        //@formatter:off
        return userService.checkToken(request)
            .flatMap(user -> userService.updateNotificationToken(user, detail))
            .map(updated -> updated > 0
                ? ok(of(0, "Okie dokie!"))
                : ok(of(0, "Couldn't update FCM token!")))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Invalid username or password!")));
        //@formatter:on
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<ResponseBody>> getUserInfo(ServerHttpRequest request) {
        return userService.checkToken(request)
            .map(user -> status(OK).body(of(0, "Okie dokie!", user)))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Your user token is invalid!")));
    }

    @PostMapping("/password")
    public Mono<ResponseEntity<ResponseBody>> changePassword(
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
    public Mono<ResponseEntity<ResponseBody>> resetPassword(@RequestBody ResetPasswordRequest detail) {
        return userService.resetPassword(detail)
            .map(reset -> reset > 0
                ? ok(of(0, "Changed password successfully!"))
                : badRequest().body(of(1, "Couldn't reset password!")))
            .doOnError(e -> log.error("Couldn't reset password {}: {}", detail, e.getMessage()))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't reset password!")));
    }

    @DeleteMapping("/token")
    public ResponseEntity<ResponseBody> signout(ServerHttpRequest request) {
        return badRequest().body(of(1, "Couldn't delete token!"));
    }

    @DeleteMapping
    public Mono<ResponseEntity<ResponseBody>> deleteUser(ServerHttpRequest request) {
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
