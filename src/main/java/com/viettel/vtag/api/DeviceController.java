package com.viettel.vtag.api;

import com.viettel.vtag.model.entity.Fence;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.model.response.JsonResponse;
import com.viettel.vtag.model.response.ObjectResponse;
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
import java.util.List;
import java.util.UUID;

import static com.viettel.vtag.model.response.ObjectResponse.of;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/device")
public class DeviceController {

    private final UserService userService;
    private final DeviceService deviceService;

    @PostMapping("/pair")
    public Mono<ResponseEntity<ObjectResponse>> pairDevice(
        @RequestBody PairDeviceRequest detail, ServerHttpRequest request
    ) {
        var userMono = userService.checkToken(request);
        return userMono.doOnNext(user -> log.info("{}: pairing to user {}", detail.platformId(), user.phone()))
            .flatMap(user -> deviceService.pairDevice(user, detail))
            .doOnNext(activated -> log.info("{}: pair {}", detail.platformId(), activated))
            .map(activated -> activated
                ? ok(of(0, "Paired device successfully!"))
                : badRequest().body(of(1, "Couldn't pair device!")))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Get lost, trespasser!")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't pair device!")));
    }

    @GetMapping(value = "/list", produces = "application/json;charset=UTF-8")
    public Mono<ResponseEntity<ObjectResponse>> getDevices(ServerHttpRequest request) {
        return userService.checkToken(request)
            .flatMap(deviceService::getDeviceList)
            .map(devices -> ok(of(0, "Okie dokie!", devices)))
            .defaultIfEmpty(status(EXPECTATION_FAILED).body(of(1, "Couldn't get user's devices")))
            .doOnError(throwable -> log.error("Couldn't get user's devices: {}", throwable.getMessage()))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't get user's devices")));
    }

    @PostMapping("/viewer")
    public Mono<ResponseEntity<ObjectResponse>> addViewer(
        @RequestBody AddViewerRequest detail, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .flatMap(user -> deviceService.addViewer(user, detail))
            .map(added -> ok(of(0, "Add viewer successfully!")))
            .defaultIfEmpty(status(EXPECTATION_FAILED).body(of(1, "Couldn't add user as viewer")))
            .doOnError(throwable -> log.error("Couldn't add user as viewer", throwable))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't add user as viewer")));
    }

    @DeleteMapping("/viewer")
    public Mono<ResponseEntity<ObjectResponse>> deleteViewer(
        @RequestBody RemoveViewerRequest detail, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .flatMap(user -> deviceService.removeViewer(user, detail))
            .filter(removed -> removed > 0)
            .map(removed -> ok(of(0, "Add viewer successfully!")))
            .defaultIfEmpty(badRequest().body(of(1, "Couldn't remove viewer")))
            .doOnError(throwable -> log.error("Couldn't remove viewer", throwable))
            .onErrorReturn(Exception.class, status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't remove viewer")));
    }

    @PutMapping("/name")
    public Mono<ResponseEntity<ObjectResponse>> updateName(
        @RequestBody ChangeDeviceNameRequest detail, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            // .map(status(UNAUTHORIZED).body(of(1, "Get lost, trespasser!")))
            .flatMap(user -> deviceService.updateDeviceName(user, detail))
            .map(added -> ok(of(0, "Changed device's name successfully!")))
            .defaultIfEmpty(status(EXPECTATION_FAILED).body(of(1, "Couldn't update device's name!")))
            .doOnError(throwable -> log.error("Couldn't update device's name!", throwable))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't update device's name!")));
    }

    @GetMapping("/history")
    public Mono<ResponseEntity<ObjectResponse>> history(
        @RequestParam("device_id") String deviceId,
        @RequestParam String from,
        @RequestParam String to,
        ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .zipWith(Mono.just(new LocationHistoryRequest().deviceId(UUID.fromString(deviceId))
                .from(LocalDateTime.parse(from))
                .to(LocalDateTime.parse(to))))
            .flatMap(req -> deviceService.fetchHistory(req.getT1(), req.getT2()))
            .map(history -> ok(of(0, "Okie dokie!", history)))
            .defaultIfEmpty(status(NOT_FOUND).body(of(1, "Couldn't find any history!")))
            .doOnError(e -> log.error("Error fetching history", e))
            .onErrorReturn(IllegalArgumentException.class, badRequest().body(of(1, "Invalid device id!")))
            .onErrorReturn(DateTimeParseException.class, badRequest().body(of(1, "Invalid date time format!")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't fetch history!")));
    }

    @PostMapping("/unpair")
    public Mono<ResponseEntity<ObjectResponse>> unpairDevice(
        @RequestBody PairDeviceRequest detail, ServerHttpRequest request
    ) {
        var userMono = userService.checkToken(request);
        return userMono.doOnNext(user -> log.info("{}: unpair from user {}", detail.platformId(), user.phone()))
            .flatMap(user -> deviceService.unpairDevice(user, detail))
            .zipWith(userMono)
            .flatMap(tuple -> tuple.getT1() ? deviceService.removeUserDevice(tuple.getT2(), detail) : Mono.just(false))
            .map(unpaired -> unpaired
                ? ok(of(0, "Unpaired device successfully!"))
                : status(BAD_GATEWAY).body(of(1, "Couldn't unpair device!")))
            .defaultIfEmpty(status(UNAUTHORIZED).body(of(1, "Get lost, trespasser!")))
            .doOnError(e -> log.error("{}: Upr ERROR", detail.platformId(), e));
    }

    @GetMapping("/messages")
    public Mono<ResponseEntity<JsonResponse>> getMessages(
        @RequestParam("device_id") String deviceId,
        @RequestParam("offset") int offset,
        @RequestParam("limit") int limit,
        ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .zipWith(Mono.fromCallable(() -> UUID.fromString(deviceId)))
            .flatMap(tuple -> deviceService.getDeviceMessages(tuple.getT1(), tuple.getT2(), offset, limit))
            .map(content -> ok(JsonResponse.of(0, "Okie dokie!", content)))
            .defaultIfEmpty(status(NO_CONTENT).body(JsonResponse.of(1, "Couldn't get any response")));
    }

    @GetMapping("/{device_id}")
    public Mono<ResponseEntity<ObjectResponse>> getDevice(
        @PathVariable("device_id") String deviceId, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .zipWith(Mono.fromCallable(() -> UUID.fromString(deviceId)))
            .doOnNext(tuple -> log.info("update geo-fencing user {}: device {}", tuple.getT1().platformId(), deviceId))
            .flatMap(tuple -> deviceService.getDevice(tuple.getT1(), tuple.getT2()))
            .doOnNext(updated -> log.info("{}: geo get {}", deviceId, updated))
            .map(content -> ok(of(0, "Okie dokie!", content)))
            .defaultIfEmpty(badRequest().body(of(1, "Couldn't get geo-fencing")))
            .doOnError(e -> log.error("{}: geo {}", deviceId, e.getMessage(), e))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't get geo-fencing")));
    }

    @GetMapping(value = "/geo/{device_id}", produces = "application/json;charset=UTF-8")
    public Mono<ResponseEntity<JsonResponse>> getGeoFencing(
        @PathVariable("device_id") String deviceId, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .zipWith(Mono.fromCallable(() -> UUID.fromString(deviceId)))
            .doOnNext(tuple -> log.info("update geo-fencing user {}: device {}", tuple.getT1().platformId(), deviceId))
            .flatMap(tuple -> deviceService.getGeoFencing(tuple.getT1(), tuple.getT2()))
            .doOnNext(updated -> log.info("{}: geo get {}", deviceId, updated))
            .map(content -> ok(JsonResponse.of(0, "Okie dokie!", content)))
            .defaultIfEmpty(badRequest().body(JsonResponse.of(1, "Couldn't get geo-fencing")))
            .doOnError(e -> log.error("{}: geo get {}", deviceId, e.getMessage(), e))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(JsonResponse.of(1, "Couldn't get geo-fencing")));
    }

    @PutMapping(value = "/geo/{device_id}", produces = "application/json;charset=UTF-8")
    public Mono<ResponseEntity<JsonResponse>> updateGeoFencingList(
        @PathVariable("device_id") String deviceId, @RequestBody List<Fence> fences, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .zipWith(Mono.fromCallable(() -> UUID.fromString(deviceId)))
            .doOnNext(tuple -> log.info("update geo-fencing user {}: device {}", tuple.getT1().platformId(), deviceId))
            .flatMap(tuple -> deviceService.updateGeofencing(tuple.getT1(), tuple.getT2(), fences))
            .doOnNext(updated -> log.info("{}: geo ({}) updated {}", deviceId, fences.size(), updated))
            .filter(updated -> updated > 0)
            .map(content -> ok(JsonResponse.of(0, "Okie dokie!")))
            .defaultIfEmpty(badRequest().body(JsonResponse.of(1, "Couldn't update geo-fencing")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(JsonResponse.of(1, "Couldn't update geo-fencing")));
    }

    @DeleteMapping("/geo/{device_id}")
    public Mono<ResponseEntity<JsonResponse>> deleteGeoFencing(
        @PathVariable("device_id") String deviceId, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .zipWith(Mono.fromCallable(() -> UUID.fromString(deviceId)))
            .doOnNext(tuple -> log.info("delete geo-fencing user {}: device {}", tuple.getT1().platformId(), deviceId))
            .flatMap(tuple -> deviceService.deleteGeofencing(tuple.getT1(), tuple.getT2()))
            .filter(updated -> updated > 0)
            .map(content -> ok(JsonResponse.of(0, "Okie dokie!")))
            .defaultIfEmpty(badRequest().body(JsonResponse.of(1, "Couldn't delete geo-fencing")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(JsonResponse.of(1, "Couldn't delete geo-fencing")));
    }

    @GetMapping("/config/{device_id}")
    public Mono<ResponseEntity<ObjectResponse>> getDeviceConfig(
        @PathVariable("device_id") String deviceId, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .zipWith(Mono.fromCallable(() -> UUID.fromString(deviceId)))
            .doOnNext(tuple -> log.info("{}: cnf {}", deviceId, tuple.getT1().platformId()))
            .flatMap(tuple -> deviceService.getConfig(tuple.getT1(), tuple.getT2()))
            .map(s -> ok(ObjectResponse.of(0, "Okie dokie!", s)))
            .defaultIfEmpty(badRequest().body(ObjectResponse.of(1, "Couldn't get device config")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(ObjectResponse.of(1, "Couldn't get device config")));
    }

    @PostMapping("/config/{device_id}")
    public Mono<ResponseEntity<JsonResponse>> configDevice(
        @PathVariable("device_id") String deviceId, @RequestBody DeviceConfig config, ServerHttpRequest request
    ) {
        return userService.checkToken(request)
            .zipWith(Mono.fromCallable(() -> UUID.fromString(deviceId)))
            .doOnNext(tuple -> log.info("{}: cnf {}", deviceId, tuple.getT1().platformId()))
            .flatMap(tuple -> deviceService.updateConfig(tuple.getT1(), tuple.getT2(), config))
            .map(updated -> updated
                ? ok(JsonResponse.of(0, "Okie dokie!"))
                : badRequest().body(JsonResponse.of(1, "Couldn't apply config")))
            .defaultIfEmpty(status(UNAUTHORIZED).body(JsonResponse.of(1, "Get lost, trespasser!")))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(JsonResponse.of(1, "Couldn't apply config")));
    }
}
