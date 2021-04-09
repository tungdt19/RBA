package com.viettel.vtag.service.interfaces;

import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public interface IotPlatformService {

    Mono<ClientResponse> get(String endpoint);

    Mono<ClientResponse> getWithToken(String endpoint);

    Mono<ClientResponse> put(String endpoint, Object body);

    Mono<ClientResponse> post(String endpoint, Object body);

    Mono<ClientResponse> delete(String endpoint);
}
