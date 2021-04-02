package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.AddViewerRequest;
import com.viettel.vtag.model.request.PairDeviceRequest;
import com.viettel.vtag.model.request.RemoveViewerRequest;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;

@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final JdbcTemplate jdbc;
    private final HttpClient httpClient;
    private final IotPlatformService iotPlatformService;

    @Value("${vtag.platform.address}")
    private String platformAddress;

    @Value("${vtag.platform.base-url}")
    private String platformUrl;

    @Value("${vtag.platform.uri}")
    private String platformUri;

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
        var sql = "DELETE FROM user_role v USING user_role o WHERE o.role_id = 1 AND o.device_id = v.device_id "
            + "AND v.phone_no = ? AND o.user_id = ?";
        return jdbc.update(sql, user.id(), detail.viewerPhone());
    }

    @Override
    public Mono<ResponseEntity<String>> convert(Object json) {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(platformUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(platformUri)
            .bodyValue(json)
            .retrieve()
            .bodyToMono(String.class)
            .map(ResponseEntity::ok);
    }

    @Override
    public int pairDevice(PairDeviceRequest request) {

        // iotPlatformService.post("/api/devices", request.toString())
        //     .flatMap(entity -> );
        return 1;
    }
}
