package com.viettel.vtag.config;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final ServerAuthenticationConverter authenticationConverter;

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        //@formatter:off
        return http.authorizeExchange().anyExchange().permitAll()
            // .authorizeExchange().pathMatchers("/", "/login", "/user/token").permitAll()
            // .and().authorizeExchange().pathMatchers("/device").hasAnyRole("ROLE_USER", "ROLE_ADMIN")
            // .and().authorizeExchange().pathMatchers("/admin").hasRole("ROLE_ADMIN")
            // .and().logout().logoutUrl("/logout")
            .and().addFilterAt(deviceAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .csrf().disable().build();
        //@formatter:on
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private AuthenticationWebFilter deviceAuthenticationFilter() {
        var filter = new AuthenticationWebFilter(new BearerTokenAuthenticationManager());

        filter.setServerAuthenticationConverter(authenticationConverter);
        filter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/device/**"));

        return filter;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    @Bean("insecure-httpclient")
    public HttpClient httpClient() throws SSLException {
        var sslContext = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
        return HttpClient.create().secure(t -> t.sslContext(sslContext));
    }
}
