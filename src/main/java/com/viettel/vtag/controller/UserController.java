package com.viettel.vtag.controller;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.ChangePasswordRequest;
import com.viettel.vtag.model.request.OtpRequest;
import com.viettel.vtag.model.request.TokenRequest;
import com.viettel.vtag.service.*;
import com.viettel.vtag.utils.TokenUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final OtpService otpService;
    private final UserService userService;

    @Qualifier("sms-service")
    private final CommunicationService smsService;

    // @Qualifier("email-service")
    // private final CommunicationService emailService;

    @PostMapping("/otp")
    public ResponseEntity<Map<String, Object>> otp(@RequestBody OtpRequest request) {
        try {
            var otp = otpService.generate();
            log.info("request {} -> otp {}", request, otp);
            var data = Map.of("otp", otp);
            switch (request.type())  {
                case "phone":
                    smsService.send("Your OTP for VTAG is " + otp, request.value());
                    break;
                case "email":
                    // emailService.send("Your OTP for VTAG is " + otp, request.value());
                    break;
            }
            return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("code", 0, "message", "Created OTP successfully!", "data", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 1, "message", String.valueOf(e.getMessage())));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        try {
            // var avatarLink = storageService.store(user.username(), avatar);
            var inserted = userService.save(user);
            if (inserted > 0) {
                return ResponseEntity.ok(Map.of("code", 0, "message", "Created user successfully!"));
            } else {
                return ResponseEntity.ok(Map.of("code", 1, "message", "Couldn't create user!"));
            }
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 1, "message", "Couldn't create user!", "data", detail));
        }
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> getToken(@RequestBody TokenRequest request) {
        var token = userService.createToken(request);
        if (token != null) {
            var data = Map.of("token", token);
            return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("code", 0, "message", "Get token successfully!", "data", data));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("code", 1, "message", "Invalid username or password!"));
        }
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
        @RequestBody ChangePasswordRequest passwordRequest, ServerHttpRequest request
    ) {
        try {
            var user = userService.checkToken(request);
            var changed = userService.changePassword(user, passwordRequest);
            if (changed > 0) {
                return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("code", 0, "message", "Changed password successfully!"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 1, "message", "Couldn't change password!"));
            }
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 1, "message", "Couldn't change password!", "data", detail));
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteUser(ServerHttpRequest request) {
        try {
            var user = userService.checkToken(request);
            var deleted = userService.delete(user);
            if (deleted > 0) {
                return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("code", 0, "message", "Delete user successfully!"));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("code", 1, "message", "Couldn't delete user!"));
            }
        } catch (Exception e) {
            var detail = Map.of("detail", String.valueOf(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 1, "message", "Couldn't delete user!", "detail", detail));
        }
    }
}
