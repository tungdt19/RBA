package com.viettel.vtag.controller;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.request.AddViewerRequest;
import com.viettel.vtag.service.DeviceService;
import com.viettel.vtag.service.IotPlatformService;
import com.viettel.vtag.service.UserService;
import com.viettel.vtag.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/device")
public class DeviceController {

    private final UserService userService;
    private final DeviceService deviceService;
    private final IotPlatformService iotService;

    @GetMapping
    public ResponseEntity<Device> getInfo(ServerHttpRequest request) {
        return null;
    }

    @PostMapping("/viewer/add")
    public ResponseEntity<Map<String, Object>> addViewer(
        @RequestBody AddViewerRequest detail, ServerHttpRequest request
    ) {
        try {
            var token = TokenUtils.getToken(request);
            var user = userService.checkToken(token);
            var inserted = deviceService.add(user, detail);
            if (inserted > 0) {
                return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("code", 0, "message", "Add viewer successfully!"));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("code", 1, "message", "Couldn't add user as viewer"));
            }
        } catch (Exception e) {
            var map = Map.of("detail", String.valueOf(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 1, "message", "Couldn't add user as viewer", "data", map));
        }
    }
}
