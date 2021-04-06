package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.*;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.repository.interfaces.LocationHistoryRepository;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final JdbcTemplate jdbc;
    private final HttpClient httpClient;
    private final IotPlatformService iotPlatformService;
    private final DeviceRepository deviceRepository;
    private final LocationHistoryRepository locationHistory;

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
    public Mono<ClientResponse> convert(String json) {
        try {
            return Mono.just(mapper.readValue(json, PlatformData.class)).flatMap(this::convert);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public Mono<ClientResponse> convert(PlatformData json) {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(convertAddress)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(convertUri)
            .bodyValue(json.token(convertToken))
            .exchange();
    }

    @Override
    public Mono<Integer> pairDevice(User user, PairDeviceRequest request) {
        var endpoint = MessageFormatter.arrayFormat("/api/devices/{}/group/{}",
            new Object[] {request.platformId(), user.platformId()}).getMessage();
        return iotPlatformService.put(endpoint, request).flatMap(response -> {
            var statusCode = response.statusCode();
            log.info("{}: {}", endpoint, statusCode);
            if (statusCode.is2xxSuccessful()) {
                var device = new Device().name("VTAG").platformId(request.platformId());
                return Mono.just(deviceRepository.save(device));
            }
            return Mono.empty();
        });
    }

    @Override
    public List<LocationHistory> fetchHistory(User user, LocationHistoryRequest detail) {
        return locationHistory.fetch(user, detail);
    }

    @Override
    public Mono<ClientResponse> active(PairDeviceRequest request) {
        log.info("/api/devices/{}/active", request.platformId());
        return iotPlatformService.post("/api/devices/" + request.platformId() + "/active", Map.of("Type", "MAD"));
    }
}
