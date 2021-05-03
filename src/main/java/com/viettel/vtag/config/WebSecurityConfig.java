package com.viettel.vtag.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
import reactor.netty.tcp.ProxyProvider;

import javax.net.ssl.SSLException;
import java.util.function.Consumer;

@Data
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final ServerAuthenticationConverter authenticationConverter;

    @Value("${vtag.proxy.host}")
    private String host;

    @Value("${vtag.proxy.port}")
    private int port;

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
        var filter = new AuthenticationWebFilter(new AuthenticationManagerConfig());

        filter.setServerAuthenticationConverter(authenticationConverter);
        filter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/device/**"));

        return filter;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    @Bean
    @Primary
    public HttpClient proxyHttpClient() {
        Consumer<ProxyProvider.TypeSpec> config = proxy -> proxy.type(ProxyProvider.Proxy.HTTP).host(host).port(port);
        return HttpClient.create().tcpConfiguration(tcpClient -> tcpClient.proxy(config));
    }

    @Bean("insecure-httpclient")
    public HttpClient httpClient() throws SSLException {
        var sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        return HttpClient.create().secure(t -> t.sslContext(sslContext))
            .tcpConfiguration(tcpClient -> tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100_000));
    }
}
