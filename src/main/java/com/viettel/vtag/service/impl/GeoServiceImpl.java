package com.viettel.vtag.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.Fence;
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
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;
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

    @Value("${vtag.unwired.base-url}")
    private String convertUrl;

    @Value("${vtag.unwired.uri}")
    private String convertUri;

    @Value("${vtag.unwired.token}")
    private String convertToken;

    @Value("${vtag.proxy.enable}")
    private boolean proxyEnable;

    @Override
    public Mono<Location> convert(UUID deviceId, WifiCellMessage json) {
        return convert(json.deviceId(deviceId)).filter(response -> {
            var status = response.statusCode();
            var ok = status.is2xxSuccessful();
            if (!ok) {
                log.info("{}: {} -> {}", deviceId, json, status);
            }
            return ok;
        }).flatMap(response -> response.bodyToMono(Location.class)).filter(location -> {
            var error = "error".equals(location.status());
            if (error) {
                log.error("{}: '{}' -> {}", deviceId, json, location);
            }
            return !error;
        });
    }

    @Override
    public Mono<ClientResponse> convert(WifiCellMessage json) {
        if (proxyEnable) {
            webClientBuilder.clientConnector(new ReactorClientHttpConnector(proxyHttpClient));
        }

        return webClientBuilder.baseUrl(convertUrl)
            .defaultHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(convertUri)
            .bodyValue(json.token(convertToken))
            .exchange();
    }

    @Override
    public Mono<Fence> checkFencing(Device device, ILocation location) {
        try {
            var fencing = device.geoFencing();
            log.info("{}: check fence {} -> {}", device.platformId(), location, fencing);
            var fences = mapper.readValue(fencing, new TypeReference<List<Fence>>() { });

            var lat = location.latitude();
            var lon = location.longitude();
            for (var fence : fences) {
                if (distance(lat, lon, fence.latitude(), fence.longitude()) <= fence.radius()) {
                    log.info("{}: fen {}", device.platformId(), fence);
                    return Mono.just(fence);
                }
            }
            return Mono.empty();
        } catch (JsonProcessingException e) {
            return Mono.empty();
        }
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
}
