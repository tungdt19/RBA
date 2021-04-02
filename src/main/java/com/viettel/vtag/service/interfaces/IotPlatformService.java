package com.viettel.vtag.service.interfaces;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface IotPlatformService {

    <T> Mono<ResponseEntity<T>> get(String endpoint, Class<T> klass);

    <T> Mono<ResponseEntity<T>> put(String endpoint, Object body, Class<T> klass);

    <T> Mono<ResponseEntity<T>> post(String endpoint, Object body, Class<T> klass);

    <T> Mono<ResponseEntity<T>> delete(String endpoint, Class<T> klass);

    default Mono<ResponseEntity<String>> get(String endpoint) {
        return get(endpoint, String.class);
    }

    default Mono<ResponseEntity<String>> put(String endpoint, Object body) {
        return put(endpoint, body, String.class);
    }

    default Mono<ResponseEntity<String>> post(String endpoint, Object body) {
        return post(endpoint, body, String.class);
    }

    default Mono<ResponseEntity<String>> delete(String endpoint) {
        return delete(endpoint, String.class);
    }
}
