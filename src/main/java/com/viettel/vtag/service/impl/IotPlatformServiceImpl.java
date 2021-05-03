package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.transfer.PlatformToken;
import com.viettel.vtag.service.interfaces.IotPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class IotPlatformServiceImpl implements IotPlatformService {

    private final ThreadPoolTaskScheduler scheduler;
    private final PlatformToken platformToken;
    private final HttpClient httpClient;

    private WebClient.Builder webClientBuilder;

    @Value("${vtag.platform.base-url}")
    private String address;

    @Value("${vtag.platform.grant-type}")
    private String grantType;

    @Value("${vtag.platform.client-id}")
    private String clientId;

    @Value("${vtag.platform.client-secret}")
    private String clientSecret;

    @Value("${vtag.mqtt.password}")
    private String deviceToken;

    public IotPlatformServiceImpl(
        ThreadPoolTaskScheduler scheduler,
        PlatformToken platformToken,
        @Qualifier("insecure-httpclient") HttpClient httpClient
    ) {
        this.scheduler = scheduler;
        this.platformToken = platformToken;
        this.httpClient = httpClient;
    }

    @PostConstruct
    public void init() {
        webClientBuilder = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(address);

        fetchToken().map(PlatformToken::expiresIn)
            .doOnNext(expire -> log.info("Setting up token schedule for each {}s", expire))
            .subscribe(expire -> {
                var trigger = new PeriodicTrigger(expire, TimeUnit.SECONDS);
                trigger.setInitialDelay(expire);
                scheduler.schedule(() -> fetchToken().subscribe(), trigger);
            });
    }

    private Mono<PlatformToken> fetchToken() {
        return webClientBuilder.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
            .build()
            .post()
            .uri("/token")
            .body(BodyInserters.fromFormData("grant_type", grantType)
                .with("client_id", clientId)
                .with("client_secret", clientSecret))
            .exchange()
            .filter(response -> response.statusCode().is2xxSuccessful())
            .flatMap(response -> response.bodyToMono(PlatformToken.class))
            .map(platformToken::update)
            .doOnNext(token -> log.info("Platform token {}", platformToken))
            .doOnError(e -> log.error("Couldn't get token from platform!", e))
            .switchIfEmpty(Mono.defer(this::fetchToken))
            .onErrorResume(e -> Mono.defer(this::fetchToken));
    }

    @Override
    public Mono<ClientResponse> get(String endpoint) {
        return webClientBuilder.build()
            .get()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .exchange();
    }

    @Override
    public Mono<ClientResponse> getWithToken(String endpoint) {
        return webClientBuilder.build()
            .get()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .header("Token", deviceToken)
            .exchange();
    }

    @Override
    public Mono<ClientResponse> put(String endpoint, Object body) {
        return webClientBuilder.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .build()
            .put()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .bodyValue(body)
            .exchange();
    }

    @Override
    public Mono<ClientResponse> post(String endpoint, Object body) {
        return webClientBuilder.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .build()
            .post()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .bodyValue(body)
            .exchange();
    }

    @Override
    public Mono<ClientResponse> delete(String endpoint) {
        return webClientBuilder.build()
            .delete()
            .uri(endpoint)
            .header("Authorization", platformToken.toString())
            .exchange();
    }
}
