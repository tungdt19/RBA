package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.Location;
import com.viettel.vtag.model.transfer.WifiCellMessage;
import com.viettel.vtag.service.interfaces.GeoConvertService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.UUID;

@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoConvertServiceImpl implements GeoConvertService {

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
        if (proxyEnable) {
            webClientBuilder.clientConnector(new ReactorClientHttpConnector(proxyHttpClient));
        }

        return webClientBuilder.baseUrl(convertUrl)
            .defaultHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(convertUri)
            .bodyValue(json.token(convertToken).deviceId(deviceId))
            .exchange()
            .filter(response -> {
                var status = response.statusCode();
                var ok = status.is2xxSuccessful();
                if (!ok) {
                    log.info("{}: {} -> {}", deviceId, json, status);
                }
                return ok;
            })
            .flatMap(response -> response.bodyToMono(Location.class))
            .filter(location -> {
                var error = "error".equals(location.status());
                if (error) {
                    log.error("{}: '{}' -> {}", deviceId, json, location);
                }
                return error;
            });
    }
}
