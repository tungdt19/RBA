package com.viettel.vtag.api;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.model.response.ResponseBody;
import com.viettel.vtag.service.impl.UserServiceImpl;
import com.viettel.vtag.service.interfaces.OtpService;
import com.viettel.vtag.service.interfaces.UserService;
import com.viettel.vtag.utils.PhoneUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

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
                return status(CONFLICT).body(new ResponseBody(1, "User's already existed!"));
            }

            otpService.sendOtp(request, otp);
            return ok(new ResponseBody(0, "Created OTP successfully!", otp));
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(new ResponseBody(1, "Couldn't create OTP", detail));
        }
    }

    @PostMapping("/otp/reset")
    public ResponseEntity<ResponseBody> resetOtp(@RequestBody OtpRequest request) {
        try {
            var otp = otpService.generateResetOtp(request);
            if (otp == null) {
                return status(NOT_FOUND).body(new ResponseBody(1, "User does not exist!"));
            }

            otpService.sendOtp(request, otp);
            var data = Map.of("otp", otp.content(), "expire", otp.expiredInstant());
            return ok(new ResponseBody(0, "Created OTP successfully!", data));
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(new ResponseBody(1, "Couldn't create OTP", detail));
        }
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<ResponseBody>> register(@RequestBody User user) {
        try {
            var phone = PhoneUtils.standardize(user.phoneNo());
            return userService.save(user.phoneNo(phone)).flatMap(inserted -> {
                if (inserted > 0) {
                    return Mono.just(ok(new ResponseBody(0, "Created user successfully!")));
                }
                return Mono.just(status(INTERNAL_SERVER_ERROR).body(new ResponseBody(1, "Couldn't create user!")));
            });
        } catch (DuplicateKeyException e) {
            log.error("Couldn't register account {}", e.getMessage());
            return Mono.just(status(CONFLICT).body(new ResponseBody(1, "User's already existed!")));
        } catch (Exception e) {
            log.error("Couldn't register account {}", e.getMessage());
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return Mono.just(status(BAD_REQUEST).body(new ResponseBody(1, "Couldn't create user!", detail)));
        }
    }

    /** {@link UserServiceImpl#createToken} */
    @PostMapping("/token")
    public ResponseEntity<ResponseBody> getToken(@RequestBody TokenRequest request) {
        var token = userService.createToken(request);
        if (token == null) {
            return status(UNAUTHORIZED).body(new ResponseBody(1, "Invalid username or password!"));
        }

        var data = Map.of("token", token);
        return ok(new ResponseBody(0, "Get token successfully!", data));
    }

    /** {@link UserServiceImpl#createToken} */
    @PostMapping("/notification")
    public ResponseEntity<ResponseBody> updateFcmToken(@RequestBody FcmTokenUpdateRequest request) {
        var token = userService.updateNotificationToken(request);
        if (token > 0) {
            var data = Map.of("token", token);
            return ok(new ResponseBody(0, "Get token successfully!", data));
        }

        return status(UNAUTHORIZED).body(new ResponseBody(1, "Invalid username or password!"));
    }

    @PostMapping("/reset")
    public ResponseEntity<ResponseBody> resetPassword(@RequestBody ResetPasswordRequest request) {
        return status(UNAUTHORIZED).body(new ResponseBody(1, "Invalid username or password!"));
    }

    @PostMapping("/password")
    public ResponseEntity<ResponseBody> changePassword(
        @RequestBody ChangePasswordRequest passwordRequest, ServerHttpRequest request
    ) {
        try {
            var user = userService.checkToken(request);
            var changed = userService.changePassword(user, passwordRequest);
            if (changed > 0) {
                return ok(new ResponseBody(0, "Changed password successfully!"));
            }

            return status(INTERNAL_SERVER_ERROR).body(new ResponseBody(1, "Couldn't change password!"));
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(new ResponseBody(1, "Couldn't change password!", detail));
        }
    }

    @DeleteMapping
    public ResponseEntity<ResponseBody> deleteUser(ServerHttpRequest request) {
        try {
            var user = userService.checkToken(request);
            var deleted = userService.delete(user);
            if (deleted > 0) {
                return ok(new ResponseBody(0, "Delete user successfully!"));
            } else {
                return ok(new ResponseBody(1, "Couldn't delete user!"));
            }
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(new ResponseBody(1, "Couldn't delete user!", detail));
        }
    }
}
