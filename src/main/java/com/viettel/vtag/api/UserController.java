package com.viettel.vtag.api;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.service.impl.UserServiceImpl;
import com.viettel.vtag.service.interfaces.OtpService;
import com.viettel.vtag.service.interfaces.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

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
    // private final User userService;

    @PostMapping("/register/otp")
    public ResponseEntity<Map<String, Object>> otp(@RequestBody OtpRequest request) {
        try {
            var otp = otpService.generate(request);
            if (otp == null) {
                return ok(Map.of("code", 1, "message", "User's already existed!"));
            }

            log.info("request {} -> otp {}", request, otp);
            otpService.sendOtp(request, otp);
            var data = Map.of("otp", otp.content(), "expire", otp.expiredInstant());
            return ok(Map.of("code", 0, "message", "Created OTP successfully!", "data", data));
        } catch (Exception e) {
            return status(INTERNAL_SERVER_ERROR).body(Map.of("code", 1, "message", String.valueOf(e.getMessage())));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        try {
            var inserted = userService.save(user);
            if (inserted > 0) {
                return ok(Map.of("code", 0, "message", "Created user successfully!"));
            } else {
                return ok(Map.of("code", 1, "message", "Couldn't create user!"));
            }
        } catch (DuplicateKeyException e) {
            log.error("Couldn't register account {}", e.getMessage());
            return status(CONFLICT).body(Map.of("code", 1, "message", "User's already existed!"));
        } catch (Exception e) {
            log.error("Couldn't register account {}", e.getMessage());
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(
                Map.of("code", 1, "message", "Couldn't create user!", "data", detail));
        }
    }

    /** {@link UserServiceImpl#createToken} */
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> getToken(@RequestBody TokenRequest request) {
        var token = userService.createToken(request);
        if (token == null) {
            return status(UNAUTHORIZED).body(Map.of("code", 1, "message", "Invalid username or password!"));
        }

        var data = Map.of("token", token);
        return ok(Map.of("code", 0, "message", "Get token successfully!", "data", data));
    }

    /** {@link UserServiceImpl#createToken} */
    @PostMapping("/sms")
    public ResponseEntity<Map<String, Object>> updateFcmToken(@RequestBody FcmTokenUpdateRequest request) {
        var token = userService.updateNotificationToken(request);
        if (token > 0) {
            var data = Map.of("token", token);
            return ok(Map.of("code", 0, "message", "Get token successfully!", "data", data));
        }

        return status(UNAUTHORIZED).body(Map.of("code", 1, "message", "Invalid username or password!"));
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
        @RequestBody ChangePasswordRequest passwordRequest, ServerHttpRequest request
    ) {
        try {
            var user = userService.checkToken(request);
            var changed = userService.changePassword(user, passwordRequest);
            if (changed > 0) {
                return ok(Map.of("code", 0, "message", "Changed password successfully!"));
            }

            return status(INTERNAL_SERVER_ERROR).body(Map.of("code", 1, "message", "Couldn't change password!"));
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(
                Map.of("code", 1, "message", "Couldn't change password!", "data", detail));
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteUser(ServerHttpRequest request) {
        try {
            var user = userService.checkToken(request);
            var deleted = userService.delete(user);
            if (deleted > 0) {
                return ok(Map.of("code", 0, "message", "Delete user successfully!"));
            } else {
                return ok(Map.of("code", 1, "message", "Couldn't delete user!"));
            }
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(
                Map.of("code", 1, "message", "Couldn't delete user!", "detail", detail));
        }
    }
}
