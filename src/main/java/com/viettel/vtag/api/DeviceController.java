package com.viettel.vtag.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.viettel.vtag.model.request.AddViewerRequest;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import com.viettel.vtag.service.interfaces.UserService;
import com.viettel.vtag.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/device")
public class DeviceController {

    private final ObjectMapper mapper = new ObjectMapper();

    private final UserService userService;
    private final DeviceService deviceService;
    private final IotPlatformService iotService;
    private final HttpClient httpClient;

    {
        mapper.registerModule(new SimpleModule().addSerializer(PlatformData.class, new CellIdSerializer()));
    }

    @PostMapping("/test")
    public Mono<ResponseEntity<String>> getInfo(@RequestBody String request) throws JsonProcessingException {
        // var request = iotService.post("/")

        var data = mapper.readValue(request, PlatformData.class);
        var json = mapper.writeValueAsString(data);

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl("https://us1.unwiredlabs.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build()
            .post()
            .uri("/v2/process.php")
            .bodyValue(json)
            .retrieve()
            .bodyToMono(String.class)
            .map(entity -> ResponseEntity.status(HttpStatus.OK).body(entity));
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

    // gateway to IoT platform is from here on
    @PostMapping("/device/")
    public ResponseEntity<String> get(@RequestBody String body) {
        return iotService.post("/device/", body).block();
    }
}
