package com.viettel.vtag.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationConverter implements ServerAuthenticationConverter {

    private static final String BEARER = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange)
            .flatMap(this::extract)
            .filter(authValue -> authValue.length() > BEARER.length())
            .map(authValue -> authValue.substring(BEARER.length()))
            .flatMap(s -> Mono.empty());
            // .flatMap(this::verifyToken)
            // .flatMap(this::buildUserDetails)
            // .flatMap(this::create);
    }

    private Mono<String> extract(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(serverWebExchange.getRequest().getHeaders().getFirst("Authorization"));
    }

    // private Mono<FirebaseToken> verifyToken(final String unverifiedToken) {
    //     try {
    //         final ApiFuture<FirebaseToken> task = firebaseAuth.verifyIdTokenAsync(unverifiedToken);
    //
    //         return Mono.justOrEmpty(task.get());
    //     } catch (final Exception e) {
    //         throw new SessionAuthenticationException(e.getMessage());
    //     }
    // }

    // private Mono<FirebaseUserDetails> buildUserDetails(final FirebaseToken firebaseToken) {
    //     return Mono.just(FirebaseUserDetails.builder()
    //         .email(firebaseToken.getEmail())
    //         .picture(firebaseToken.getPicture())
    //         .userId(firebaseToken.getUid())
    //         .username(firebaseToken.getName())
    //         .build());
    // }

    private Mono<Authentication> create(UserDetails userDetails) {
        return Mono.justOrEmpty(
            new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities()));
    }
}
