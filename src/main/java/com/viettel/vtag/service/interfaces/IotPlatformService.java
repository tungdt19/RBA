package com.viettel.vtag.service.interfaces;

import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public interface IotPlatformService {

    default <T> Mono<T> get(String endpoint, Class<T> klass) {
        return get(endpoint).flatMap(response -> response.bodyToMono(klass));
    }

    Mono<ClientResponse> get(String endpoint);

    default <T> Mono<T> put(String endpoint, Object body, Class<T> klass) {
        return put(endpoint, body).flatMap(response -> response.bodyToMono(klass));
    }

    Mono<ClientResponse> put(String endpoint, Object body);

    default <T> Mono<T> post(String endpoint, Object body, Class<T> klass) {
        return post(endpoint, body).flatMap(response -> response.bodyToMono(klass));
    }

    Mono<ClientResponse> post(String endpoint, Object body);

    default <T> Mono<T> delete(String endpoint, Class<T> klass) {
        return delete(endpoint).flatMap(response -> response.bodyToMono(klass));
    }

    Mono<ClientResponse> delete(String endpoint);
}
