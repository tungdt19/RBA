package com.viettel.vtag.api;

import com.viettel.vtag.model.response.ResponseBody;
import com.viettel.vtag.service.interfaces.AdminDeviceService;
import com.viettel.vtag.service.interfaces.DeviceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.viettel.vtag.model.response.ResponseBody.of;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.*;

@Data
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final DeviceService deviceService;

    @GetMapping("/devices")
    public Mono<ResponseEntity<ResponseBody>> getAllDevices() {
        return deviceService.getAllDevices()
            .doOnNext(list -> log.info("Get all {} devices", list.size()))
            .map(list -> ok(of(0, "Okie", list)))
            .defaultIfEmpty(notFound().build())
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't get all devices")));
    }

    @GetMapping("/history/{device-id}")
    public Mono<ResponseEntity<ResponseBody>> getDeviceHistory(@PathVariable("device-id") String deviceId) {
        return null;
    }
}
