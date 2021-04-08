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

import java.util.Map;

import static com.viettel.vtag.model.response.ResponseBody.of;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final OtpService otpService;
    private final UserService userService;

    @PostMapping("/otp/register")
    public ResponseEntity<ResponseBody> registerOtp(@RequestBody OtpRequest request) {
        try {
            var otp = otpService.generateRegisterOtp(request);
            if (otp == null) {
                return status(CONFLICT).body(of(1, "User's already existed!"));
            }

            otpService.sendOtp(request, otp);
            return ok(of(0, "Created OTP successfully!", otp));
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't create OTP", detail));
        }
    }

    /** {@link UserServiceImpl#save(User)} */
    @PostMapping("/register")
    public Mono<ResponseEntity<ResponseBody>> register(@RequestBody User user) {
        return userService.save(user).map(registered -> {
            switch (registered) {
                case 1:
                    return ok(of(0, "Created user successfully!"));
                case -1:
                    return status(CONFLICT).body(of(1, "User's already existed!"));
                default:
                    return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't create user!"));
            }
        }).onErrorReturn(status(BAD_REQUEST).body(of(1, "Couldn't create user!")));
    }

    @PostMapping("/otp/reset")
    public ResponseEntity<ResponseBody> resetOtp(@RequestBody OtpRequest request) {
        try {
            var otp = otpService.generateResetOtp(request);
            if (otp == null) {
                return status(NOT_FOUND).body(of(1, "User does not exist!"));
            }

            otpService.sendOtp(request, otp);
            var data = Map.of("otp", otp.content(), "expire", otp.expiredInstant());
            return ok(of(0, "Created OTP successfully!", data));
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't create OTP", detail));
        }
    }

    /** {@link UserServiceImpl#createToken} */
    @PostMapping("/token")
    public ResponseEntity<ResponseBody> getToken(@RequestBody TokenRequest request) {
        var token = userService.createToken(request);
        if (token == null) {
            return status(UNAUTHORIZED).body(of(1, "Invalid username or password!"));
        }

        return ok(of(0, "Get token successfully!", Map.of("token", token)));
    }

    /** {@link UserServiceImpl#updateNotificationToken(User, FcmTokenUpdateRequest)} */
    @PostMapping("/notification")
    public ResponseEntity<ResponseBody> updateFcmToken(
        @RequestBody FcmTokenUpdateRequest detail, ServerHttpRequest request
    ) {
        var user = userService.checkToken(request);
        var updated = userService.updateNotificationToken(user, detail);
        if (updated > 0) {
            return ok(of(0, "Get token successfully!", Map.of("token", updated)));
        }

        return status(UNAUTHORIZED).body(of(1, "Invalid username or password!"));
    }

    @GetMapping("/info")
    public ResponseEntity<ResponseBody> getUserInfo(ServerHttpRequest request) {
        var user = userService.checkToken(request);
        return status(OK).body(of(1, "Okie dokie!", user));
    }

    @PostMapping("/password")
    public ResponseEntity<ResponseBody> changePassword(
        @RequestBody ChangePasswordRequest passwordRequest, ServerHttpRequest request
    ) {
        try {
            var user = userService.checkToken(request);
            var changed = userService.changePassword(user, passwordRequest);
            if (changed > 0) {
                return ok(of(0, "Changed password successfully!"));
            }

            return status(BAD_REQUEST).body(of(1, "Couldn't change password!"));
        } catch (Exception e) {
            log.error("change password", e);
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't change password!", detail));
        }
    }

    /** {@see UserServiceImpl#resetPassword} */
    @PostMapping("/password/reset")
    public ResponseEntity<ResponseBody> resetPassword(
        @RequestBody ResetPasswordRequest detail, ServerHttpRequest request
    ) {
        try {
            var reset = userService.resetPassword(detail);
            if (reset > 0) {
                return ok(of(0, "Changed password successfully!"));
            }

            return status(BAD_REQUEST).body(of(1, "Couldn't change password!"));
        } catch (Exception e) {
            log.error("Couldn't change password", e);
            return status(INTERNAL_SERVER_ERROR).body(
                of(1, "Couldn't change password!", Map.of("detail", String.valueOf(e.getMessage()))));
        }
    }

    @DeleteMapping("/token")
    public ResponseEntity<ResponseBody> signout(ServerHttpRequest request) {
        return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't delete token!"));
    }

    @DeleteMapping
    public ResponseEntity<ResponseBody> deleteUser(ServerHttpRequest request) {
        try {
            var user = userService.checkToken(request);
            var deleted = userService.delete(user);
            if (deleted > 0) {
                return ok(of(0, "Delete user successfully!"));
            } else {
                return ok(of(1, "Couldn't delete user!"));
            }
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't delete user!", detail));
        }
    }
}
