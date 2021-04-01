package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.transfer.PlatformToken;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class IotPlatformServiceImpl implements IotPlatformService {

    @Value("${vtag.platform.address}")
    private String address;
    @Value("${vtag.platform.grant-type}")
    private String grantType;
    @Value("${vtag.platform.client-id}")
    private String clientId;
    @Value("${vtag.platform.client-secret}")
    private String clientSecret;

    private final ThreadPoolTaskScheduler taskScheduler;
    private final PlatformToken platformToken;
    private final HttpClient httpClient;

    @PostConstruct
    public void init() {
        fetchToken().subscribe(entity -> {
            if (entity == null || entity.getBody() == null) {
                log.error("Couldn't get token from platform");
                return;
            }

            platformToken.update(entity.getBody());
            taskScheduler.schedule(() -> {
                var response = fetchToken().block();
                if (response == null || response.getBody() == null) {
                    log.error("Couldn't get token from platform!");
                    return;
                }
                platformToken.update(response.getBody());
            }, new PeriodicTrigger(platformToken.expiresIn() * 1000));
        });
    }

    private WebClient.Builder webClientBuilder() {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).baseUrl(address);
    }

    private Mono<ResponseEntity<PlatformToken>> fetchToken() {
        var client = webClientBuilder().defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE).build();

        var body = BodyInserters.fromFormData("grant_type", grantType)
            .with("client_id", clientId)
            .with("client_secret", clientSecret);

        return client.post().uri("/token").body(body).retrieve().toEntity(PlatformToken.class);
    }

    @Override
    public Mono<ResponseEntity<String>> get(String endpoint) {
        var client = webClientBuilder().build();
        return client.get()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .retrieve()
            .toEntity(String.class);
    }

    @Override
    public Mono<ResponseEntity<String>> put(String endpoint, String body) {
        var client = webClientBuilder().defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE).build();

        return client.put()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .bodyValue(body)
            .retrieve()
            .toEntity(String.class);
    }

    @Override
    public Mono<ResponseEntity<String>> post(String endpoint, String body) {
        var client = webClientBuilder().defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE).build();

        return client.post()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .bodyValue(body)
            .retrieve()
            .toEntity(String.class);
    }
}
