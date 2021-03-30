package com.viettel.vtag.service.interfaces;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface IotPlatformService {

    Mono<ResponseEntity<String>> get(String endpoint);

    Mono<ResponseEntity<String>> put(String endpoint, String body);

    Mono<ResponseEntity<String>> post(String endpoint, String body);
}
