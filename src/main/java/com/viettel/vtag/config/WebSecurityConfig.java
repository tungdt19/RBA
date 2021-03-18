package com.viettel.vtag.config;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        //@formatter:off
        return http.csrf().disable().authorizeExchange().anyExchange().permitAll()
            // .authorizeExchange().pathMatchers("/", "/login", "/user/token").permitAll()
            // .and().authorizeExchange().pathMatchers("/device").hasAnyRole("ROLE_USER", "ROLE_ADMIN")
            // .and().authorizeExchange().pathMatchers("/admin").hasRole("ROLE_ADMIN")
            // .and().logout().logoutUrl("/logout")
            .and().build();
        //@formatter:on
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    @Bean
    public HttpClient httpClient() throws SSLException {
        var sslContext = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
        return HttpClient.create().secure(t -> t.sslContext(sslContext));
    }
}
