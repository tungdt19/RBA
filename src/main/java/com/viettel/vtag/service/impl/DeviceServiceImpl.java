package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.AddViewerRequest;
import com.viettel.vtag.model.request.PairDeviceRequest;
import com.viettel.vtag.model.request.RemoveViewerRequest;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final JdbcTemplate jdbc;
    private final HttpClient httpClient;
    private final IotPlatformService iotPlatformService;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${vtag.unwired.base-url}")
    private String convertAddress;

    @Value("${vtag.unwired.uri}")
    private String convertUri;

    @Value("${vtag.unwired.token}")
    private String convertToken;

    @Override
    public int addViewer(User user, AddViewerRequest request) {
        var sql = "INSERT INTO user_role(user_id, device_id, role_id) SELECT ?, id, ? FROM device d WHERE imei = ?";
        return jdbc.update(sql, user.id(), request.imei(), "ROLE_VIEWER");
    }

    @Override
    public String getInfo(String deviceId) {
        //TODO: implement this
        return null;
    }

    @Override
    public List<Device> getList(User user) {
        var sql = "SELECT id, name, imei FROM device JOIN user_role ur ON device.id = ur.device_id WHERE user_id = ?";
        return jdbc.query(sql, new Object[] {user.id()},
            (rs, rowNum) -> new Device().id(rs.getInt("id")).name(rs.getString("name")).imei(rs.getString("imei")));
    }

    @Override
    public int remove(User user, RemoveViewerRequest detail) {
        var sql = "DELETE FROM user_role v USING user_role o RIGHT JOIN end_user u ON v.user_id = u.id "
            + "WHERE o.role_id = 1 AND o.device_id = v.device_id AND u.phone_no = ? AND o.user_id = ?";
        return jdbc.update(sql, detail.viewerPhone(), user.id());
    }

    @Override
    public Mono<ResponseEntity<String>> convert(String json) {
        try {
            var platformData = mapper.readValue(json, PlatformData.class);
            return convert(platformData);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public Mono<ResponseEntity<String>> convert(PlatformData json) {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(convertAddress)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(convertUri)
            .bodyValue(json.token(convertToken))
            .retrieve()
            .bodyToMono(String.class)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<Integer> pairDevice(PairDeviceRequest request) {
        return iotPlatformService.post("/api/devices", request, PlatformDevice.class).flatMap(entity -> {
            if (entity.getStatusCode() != CREATED) return Mono.empty();

            var platformDevice = entity.getBody();
            if (platformDevice == null) return Mono.empty();

            iotPlatformService.post("/api/devices/" + platformDevice.id() + "/group/" + platformDevice.groupId(), request)
                .flatMap(response -> {
                    if (entity.getStatusCode() == OK) return Mono.empty();
                    var device = new Device().name(request.name())
                        .imei(request.imei())
                        .platformId(platformDevice.id().toString());
                    return Mono.just(deviceRepository.save(device));
                });

            return Mono.empty();
        });
    }
}
