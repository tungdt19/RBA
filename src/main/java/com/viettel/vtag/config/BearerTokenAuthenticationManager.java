package com.viettel.vtag.config;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BearerTokenAuthenticationManager implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!authentication.isAuthenticated()) return Mono.empty();

        return Mono.justOrEmpty(authentication);
    }
}
