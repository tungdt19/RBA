package com.viettel.vtag.api;

import com.viettel.vtag.model.response.JsonResponse;
import com.viettel.vtag.model.response.ObjectResponse;
import com.viettel.vtag.service.interfaces.AdminDeviceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.viettel.vtag.model.response.ObjectResponse.of;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.*;

@Data
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/device")
public class AdminDeviceController {

    private final AdminDeviceService deviceService;

    @GetMapping("/all")
    public Mono<ResponseEntity<ObjectResponse>> getAllDevices() {
        return deviceService.getAllDevices()
            .doOnNext(list -> log.info("Get all {} devices", list.size()))
            .map(list -> ok(of(0, "Okie", list)))
            // .defaultIfEmpty(notFound().build())
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't get all devices")));
    }

    @GetMapping(value = "/{device-id}", produces = "application/json;charset=UTF-8")
    public Mono<ResponseEntity<JsonResponse>> getDevice(@PathVariable("device-id") UUID deviceId) {
        return null;
    }

    @GetMapping(value = "/platform/all", produces = "application/json;charset=UTF-8")
    public Mono<ResponseEntity<JsonResponse>> getAllDeviceFromPlatform() {
        return deviceService.getAllPlatformDevices().map(content -> ok(JsonResponse.of(0, "Okie dokie!", content)));
    }

    @GetMapping("/history/{device-id}")
    public Mono<ResponseEntity<ObjectResponse>> getDeviceHistory(@PathVariable("device-id") String deviceId) {
        return Mono.justOrEmpty(UUID.fromString(deviceId))
            .flatMap(deviceService::getDeviceHistory)
            .doOnNext(list -> log.info("{}: his {} points", deviceId, list.size()))
            .map(list -> ok(of(0, "Okie", list)))
            .defaultIfEmpty(notFound().build())
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't get all devices")));
    }
}
