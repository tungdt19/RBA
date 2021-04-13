package com.viettel.vtag.api;

import com.viettel.vtag.model.entity.Fencing;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.model.response.ResponseBody;
import com.viettel.vtag.model.response.ResponseJson;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.viettel.vtag.model.response.ResponseBody.of;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/device")
public class DeviceController {

    private final UserService userService;
    private final DeviceService deviceService;

    @GetMapping("/list")
    public Mono<ResponseEntity<ResponseBody>> getDevices(ServerHttpRequest request) {
        return Mono.justOrEmpty(userService.checkToken(request))
            .flatMap(deviceService::getList)
            .map(devices -> ok(of(0, "Okie dokie!", devices)))
            .defaultIfEmpty(status(EXPECTATION_FAILED).body(of(1, "Couldn't get user's devices")))
            .doOnError(throwable -> log.error("Couldn't add user", throwable))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't get user's devices")));
    }

    @PostMapping("/viewer")
    public Mono<ResponseEntity<ResponseBody>> addViewer(
        @RequestBody AddViewerRequest detail, ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(userService.checkToken(request))
            .flatMap(user -> deviceService.addViewer(user, detail))
            .map(added -> ok(of(0, "Add viewer successfully!")))
            .defaultIfEmpty(status(EXPECTATION_FAILED).body(of(1, "Couldn't add user as viewer")))
            .doOnError(throwable -> log.error("Couldn't add user", throwable))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't add user as viewer")));
    }

    @DeleteMapping("/viewer")
    public Mono<ResponseEntity<ResponseBody>> deleteViewer(
        @RequestBody RemoveViewerRequest detail, ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(userService.checkToken(request))
            .flatMap(user -> deviceService.removeViewer(user, detail))
            .filter(removed -> removed > 0)
            .map(removed -> ok(of(0, "Add viewer successfully!")))
            .defaultIfEmpty(status(BAD_REQUEST).body(of(1, "Couldn't remove viewer")))
            .doOnError(throwable -> log.error("Couldn't add user", throwable))
            .onErrorReturn(Exception.class, status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't remove viewer")));
    }

    @PostMapping("/pair")
    public Mono<ResponseEntity<ResponseBody>> pairDevice(
        @RequestBody PairDeviceRequest detail, ServerHttpRequest request
    ) {
        var user = userService.checkToken(request);
        return Mono.justOrEmpty(user)
            .flatMap(usr -> deviceService.pairDevice(user, detail))
            .doOnNext(response -> log.info("{}: paired to user {}", detail.platformId(), user.phone()))
            .flatMap(response -> deviceService.saveUserDevice(user, detail))
            .map(activated -> ok(of(0, "Paired device successfully!")))
            .defaultIfEmpty(status(BAD_GATEWAY).body(of(1, "Couldn't pair device!")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't pair device!")));
    }

    @PutMapping("/name")
    public Mono<ResponseEntity<ResponseBody>> updateName(
        @RequestBody ChangeDeviceNameRequest detail, ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(userService.checkToken(request))
            // .map(status(UNAUTHORIZED).body(of(1, "Get lost, trespasser!")))
            .flatMap(user -> deviceService.updateDeviceName(user, detail))
            .map(added -> ok(of(0, "Changed device's name successfully!")))
            .defaultIfEmpty(status(EXPECTATION_FAILED).body(of(1, "Couldn't update device's name!")))
            .doOnError(throwable -> log.error("Couldn't update device's name!", throwable))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't update device's name!")));
    }

    @GetMapping("/history")
    public Mono<ResponseEntity<ResponseBody>> history(
        @RequestParam("device_id") String deviceId,
        @RequestParam String from,
        @RequestParam String to,
        ServerHttpRequest request
    ) {
        try {
            var requestMono = Mono.just(new LocationHistoryRequest().deviceId(UUID.fromString(deviceId))
                .from(LocalDateTime.parse(from))
                .to(LocalDateTime.parse(to)));
            return Mono.justOrEmpty(userService.checkToken(request))
                .zipWith(requestMono)
                .flatMap(req -> deviceService.fetchHistory(req.getT1(), req.getT2()))
                .map(history -> ok(of(0, "Okie dokie!", history)))
                .defaultIfEmpty(status(NOT_FOUND).body(of(1, "Couldn't find any history!")))
                .doOnError(e -> log.error("Error fetching history", e))
                .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't fetch history!")));
        } catch (IllegalArgumentException e) {
            return Mono.just(badRequest().body(of(1, "Invalid device id!")));
        } catch (DateTimeParseException e) {
            return Mono.just(badRequest().body(of(1, "Invalid date time format!")));
        }
    }

    @PostMapping("/unpair")
    public Mono<ResponseEntity<ResponseBody>> unpairDevice(
        @RequestBody PairDeviceRequest detail, ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(userService.checkToken(request))
            .doOnNext(user -> log.info("unpair device {} from user {}", detail.platformId(), user.phone()))
            .flatMap(user -> deviceService.unpairDevice(user, detail))
            .then(deviceService.deactivate(detail))
            .map(bool -> true)
            .map(activated -> ok(of(0, "Unpaired device successfully!")))
            .doOnError(e -> log.error("Error on unpair {}", detail.platformId(), e))
            .defaultIfEmpty(status(BAD_GATEWAY).body(of(1, "Couldn't unpair device!")));
    }

    @GetMapping("/messages")
    public Mono<ResponseEntity<ResponseJson>> getMessages(
        @RequestParam("device_id") String deviceId,
        @RequestParam("offset") int offset,
        @RequestParam("limit") int limit,
        ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(userService.checkToken(request))
            .zipWith(Mono.just(UUID.fromString(deviceId)))
            .flatMap(tuple -> deviceService.getMessages(tuple.getT1(), tuple.getT2(), offset, limit))
            .map(content -> ok(ResponseJson.of(0, "Okie dokie!").json(content)))
            .defaultIfEmpty(status(NO_CONTENT).body(ResponseJson.of(1, "Couldn't get any response")));
    }

    @PutMapping("/geo/{device_id}")
    public Mono<ResponseEntity<ResponseJson>> updateGeoFencingList(
        @PathVariable("device_id") String deviceId, @RequestBody List<Fencing> fencing, ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(userService.checkToken(request))
            .zipWith(Mono.just(UUID.fromString(deviceId)))
            .doOnNext(tuple -> log.info("update geo-fencing user {}: device {}", tuple.getT1().platformId(), deviceId))
            .flatMap(tuple -> deviceService.updateGeofencing(tuple.getT1(), tuple.getT2(), fencing))
            .doOnNext(updated -> log.info("{}: geo updated {}", deviceId, updated))
            .filter(updated -> updated > 0)
            .map(content -> ok(ResponseJson.of(0, "Okie dokie!")))
            .defaultIfEmpty(status(BAD_REQUEST).body(ResponseJson.of(1, "Couldn't update geo-fencing")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(ResponseJson.of(1, "Couldn't update geo-fencing")));
    }

    @DeleteMapping("/geo/{device_id}")
    public Mono<ResponseEntity<ResponseJson>> deleteGeoFencing(
        @PathVariable("device_id") String deviceId, ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(userService.checkToken(request))
            .zipWith(Mono.just(UUID.fromString(deviceId)))
            .doOnNext(tuple -> log.info("delete geo-fencing user {}: device {}", tuple.getT1().platformId(), deviceId))
            .flatMap(tuple -> deviceService.deleteGeofencing(tuple.getT1(), tuple.getT2()))
            .filter(updated -> updated > 0)
            .map(content -> ok(ResponseJson.of(0, "Okie dokie!")))
            .defaultIfEmpty(status(BAD_REQUEST).body(ResponseJson.of(1, "Couldn't delete geo-fencing")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(ResponseJson.of(1, "Couldn't delete geo-fencing")));
    }
}
