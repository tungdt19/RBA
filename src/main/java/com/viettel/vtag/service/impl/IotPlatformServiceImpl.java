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
import org.springframework.web.reactive.function.client.ClientResponse;
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

    private final ThreadPoolTaskScheduler taskScheduler;
    private final PlatformToken platformToken;
    private final HttpClient httpClient;

    @Value("${vtag.platform.base-url}")
    private String address;
    @Value("${vtag.platform.grant-type}")
    private String grantType;
    @Value("${vtag.platform.client-id}")
    private String clientId;
    @Value("${vtag.platform.client-secret}")
    private String clientSecret;

    @PostConstruct
    public void init() {
        fetchToken().subscribe(entity -> {
            if (entity == null || entity.getBody() == null) {
                log.error("Couldn't get token from platform");
                return;
            }

            platformToken.update(entity.getBody());
            log.info("Platform token {}", platformToken);
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

    private Mono<ResponseEntity<PlatformToken>> fetchToken() {
        var client = webClientBuilder().defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE).build();

        var body = BodyInserters.fromFormData("grant_type", grantType)
            .with("client_id", clientId)
            .with("client_secret", clientSecret);

        return client.post().uri("/token").body(body).retrieve().toEntity(PlatformToken.class);
    }

    private WebClient.Builder webClientBuilder() {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).baseUrl(address);
    }

    @Override
    public Mono<ClientResponse> get(String endpoint) {
        var client = webClientBuilder().build();
        return client.get().uri(endpoint).header("Authorization", platformToken.toString()).exchange();
    }

    @Override
    public Mono<ClientResponse> put(String endpoint, Object body) {
        return webClientBuilder().defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .build()
            .put()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .bodyValue(body)
            .exchange();
    }

    @Override
    public Mono<ClientResponse> post(String endpoint, Object body) {
        return webClientBuilder().defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .bodyValue(body)
            .exchange();
    }

    @Override
    public Mono<ClientResponse> delete(String endpoint) {
        return webClientBuilder().defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .build()
            .delete()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .exchange();
    }
}
