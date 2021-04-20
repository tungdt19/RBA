package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.FenceCheck;
import com.viettel.vtag.model.entity.Location;
import com.viettel.vtag.model.transfer.WifiCellMessage;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
import com.viettel.vtag.service.interfaces.GeoService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.UUID;

import static java.lang.Math.*;

@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoServiceImpl implements GeoService {

    private static final double R = 6_371_000;
    private static final double radian = PI / 180;

    private final ObjectMapper mapper = new ObjectMapper();

    private final DeviceRepository deviceRepository;
    private final WebClient.Builder webClientBuilder;
    private final HttpClient proxyHttpClient;

    @Value("${vtag.proxy.enable}")
    private boolean proxyEnable;

    @Value("${vtag.unwired.base-url}")
    private String convertUrl;

    @Value("${vtag.unwired.uri}")
    private String convertUri;

    @Value("${vtag.unwired.token}")
    private String convertToken;

    @Override
    public Mono<Location> convert(WifiCellMessage json) {
        return null;
    }

    @Override
    public Mono<Location> convert(UUID deviceId, WifiCellMessage json) {
        return convert(deviceId, json, convertToken);
        // .switchIfEmpty(Mono.defer(() -> convert(deviceId, json.deviceId(deviceId), backupToken)));
    }

    @Override
    public FenceCheck checkFencing(Device device, ILocation location) {
        var fences = device.fences();
        if (fences == null) return FenceCheck.NOT_CHANGE;

        var lat = location.latitude();
        var lon = location.longitude();
        device.latitude(lat).longitude(lon);
        var check = new FenceCheck();
        for (var fence : fences) {
            var distance = distance(lat, lon, fence.latitude(), fence.longitude());
            boolean inFence = distance <= fence.radius();
            if (fence.in() == null) {
                fence.in(inFence);
                continue;
            }
            if (inFence) {
                if (!fence.in()) {
                    log.info("{}: in {}", device.platformId(), fence);
                    check.to(fence);
                }
            } else if (fence.in()) {
                check.from(fence);
                log.info("{}: out {}", device.platformId(), fence);
            }
            fence.in(inFence);
        }
        return check.device(device).location(location).build();
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        var phi1 = lat1 * radian;
        var phi2 = lat2 * radian;
        var deltaPhi = (lat2 - lat1) * radian / 2;
        var deltaLambda = (lon2 - lon1) * radian / 2;
        var sinPhi = sin(deltaPhi);
        var sinLambda = sin(deltaLambda);
        var a = sinPhi * sinPhi + cos(phi1) * cos(phi2) * sinLambda * sinLambda;
        return 2 * R * atan2(sqrt(a), sqrt(1 - a));
    }

    public Mono<Location> convert(UUID deviceId, WifiCellMessage json, String token) {
        if (proxyEnable) {
            webClientBuilder.clientConnector(new ReactorClientHttpConnector(proxyHttpClient));
        }

        return webClientBuilder.baseUrl(convertUrl)
            .defaultHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(convertUri)
            .bodyValue(json.token(token))
            .exchange()
            .filter(response -> {
                var status = response.statusCode();
                if (status.is2xxSuccessful()) return true;

                try {
                    log.info("{}: ({}) {} -> {}", deviceId, token, mapper.writeValueAsString(json), status);
                } catch (JsonProcessingException e) {
                    log.info("{}: ({}) {} -> {}", deviceId, token, json, status);
                }

                return false;
            })
            .flatMap(response -> response.bodyToMono(Location.class))
            .filter(location -> {
                if ("ok".equals(location.status())) return true;

                try {
                    log.info("{}: ({}) '{}' -> {}", deviceId, token, mapper.writeValueAsString(json), location);
                } catch (JsonProcessingException e) {
                    log.info("{}: ({}) '{}' -> {}", deviceId, token, json, location);
                }

                return false;
            });
    }
}
