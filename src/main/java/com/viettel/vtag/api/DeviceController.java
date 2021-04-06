package com.viettel.vtag.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.viettel.vtag.model.entity.PlatformData;
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

import java.util.Map;

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
        mapper.registerModule(new SimpleModule().addSerializer(PlatformData.class, new CellIdSerializer()));
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseBody> getDevices(ServerHttpRequest request) {
        try {
            var token = TokenUtils.getToken(request);
            var user = userService.checkToken(token);
            var deviceList = deviceService.getList(user);
            return ok(of(0, "Okie dokie!", deviceList));
        } catch (Exception e) {
            var map = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't add user as viewer", map));
        }
    }

    @PostMapping("/viewer")
    public ResponseEntity<ResponseBody> addViewer(
        @RequestBody AddViewerRequest detail, ServerHttpRequest request
    ) {
        try {
            var token = TokenUtils.getToken(request);
            var user = userService.checkToken(token);
            var inserted = deviceService.addViewer(user, detail);
            if (inserted > 0) {
                return ok(of(0, "Add viewer successfully!"));
            } else {
                return ok(of(1, "Couldn't add user as viewer"));
            }
        } catch (Exception e) {
            var map = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't add user as viewer", map));
        }
    }

    @DeleteMapping("/viewer")
    public ResponseEntity<ResponseBody> deleteViewer(
        @RequestBody RemoveViewerRequest detail, ServerHttpRequest request
    ) {
        try {
            var token = TokenUtils.getToken(request);
            var user = userService.checkToken(token);
            var removed = deviceService.remove(user, detail);
            if (removed > 0) {
                return ok(of(0, "Add viewer successfully!"));
            } else {
                return status(BAD_REQUEST).body(of(1, "Couldn't remove viewer"));
            }
        } catch (Exception e) {
            var map = Map.of("detail", String.valueOf(e.getMessage()));
            return status(INTERNAL_SERVER_ERROR).body(of(1, "Couldn't add user as viewer", map));
        }
    }

    // gateway to IoT platform is from here on
    @PostMapping("/pair")
    public Mono<ResponseEntity<ResponseBody>> pairDevice(
        @RequestBody PairDeviceRequest detail, ServerHttpRequest request
    ) {
        var token = TokenUtils.getToken(request);
        var user = userService.checkToken(token);
        log.info("pair user: {}", user);
        return deviceService.pairDevice(user, detail)
            .flatMap(paired -> deviceService.active(detail))
            .map(response -> response.statusCode().is2xxSuccessful())
            .map(paired -> paired
                ? ok(of(0, "Paired device successfully!"))
                : status(BAD_GATEWAY).body(of(1, "Couldn't pair device!")));
    }

    @GetMapping("/history")
    public Mono<ResponseEntity<ResponseBody>> history(
        @RequestBody LocationHistoryRequest detail, ServerHttpRequest request
    ) {
        var token = TokenUtils.getToken(request);
        var user = userService.checkToken(token);
        var history = deviceService.fetchHistory(user, detail);

        if (history.isEmpty()) {
            return Mono.just(status(NOT_FOUND).body(of(1, "Couldn't pair device!")));
        }
        return Mono.just(ok(of(0, "Okie dokie!", history)));
    }
}
