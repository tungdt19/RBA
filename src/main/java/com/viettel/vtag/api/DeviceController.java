package com.viettel.vtag.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.viettel.vtag.model.transfer.LocationMessage;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.model.response.ResponseBody;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.UserService;
import com.viettel.vtag.utils.CellIdSerializer;
import com.viettel.vtag.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.viettel.vtag.model.response.ResponseBody.of;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/device")
public class DeviceController {

    private final ObjectMapper mapper = new ObjectMapper();

    private final UserService userService;
    private final DeviceService deviceService;

    {
        mapper.registerModule(new SimpleModule().addSerializer(LocationMessage.class, new CellIdSerializer()));
    }

    @GetMapping("/list")
    public Mono<ResponseEntity<ResponseBody>> getDevices(ServerHttpRequest request) {
        return Mono.justOrEmpty(TokenUtils.getToken(request))
            .map(userService::checkToken)
            .flatMap(deviceService::getList)
            .map(devices -> ok(of(0, "Okie dokie!", devices)))
            .defaultIfEmpty(status(EXPECTATION_FAILED).body(of(1, "Couldn't get user's device")))
            .doOnError(throwable -> log.error("Couldn't add user", throwable))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't add user as viewer")));
    }

    @PostMapping("/viewer")
    public Mono<ResponseEntity<ResponseBody>> addViewer(
        @RequestBody AddViewerRequest detail, ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(TokenUtils.getToken(request))
            .map(userService::checkToken)
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
        return Mono.justOrEmpty(TokenUtils.getToken(request))
            .map(userService::checkToken)
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
        return Mono.justOrEmpty(TokenUtils.getToken(request))
            .map(userService::checkToken)
            .map(user -> {
                log.info("pair device {} to user {}", detail.platformId(), user);
                return user;
            })
            .flatMap(user -> deviceService.pairDevice(user, detail))
            .then(deviceService.activate(detail))
            .map(activated -> ok(of(0, "Paired device successfully!")))
            .defaultIfEmpty(status(BAD_GATEWAY).body(of(1, "Couldn't pair device!")));
    }

    @PutMapping("/name")
    public Mono<ResponseEntity<ResponseBody>> updateName(
        @RequestBody ChangeDeviceNameRequest detail, ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(TokenUtils.getToken(request))
            .map(userService::checkToken)
            .flatMap(user -> deviceService.updateDeviceName(user, detail))
            .map(added -> ok(of(0, "Changed device's name successfully!")))
            .defaultIfEmpty(status(EXPECTATION_FAILED).body(of(1, "Couldn't update device's name!")))
            .doOnError(throwable -> log.error("Couldn't add user", throwable))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't update device's name!")));
    }

    @GetMapping("/history")
    public Mono<ResponseEntity<ResponseBody>> history(
        @RequestBody LocationHistoryRequest detail, ServerHttpRequest request
    ) {
        return Mono.justOrEmpty(TokenUtils.getToken(request))
            .map(userService::checkToken)
            .flatMap(user -> deviceService.fetchHistory(user, detail))
            .map(history -> ok(of(0, "Okie dokie!", history)))
            .defaultIfEmpty(status(NOT_FOUND).body(of(1, "Couldn't fetch history!")))
            .doOnError(e -> log.error("Couldn't fetch history", e))
            .onErrorReturn(status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't fetch history!")));
    }
}
