package com.viettel.vtag.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${vtag.proxy.enable}")
    private boolean proxyEnable;

    @Value("${vtag.proxy.host}")
    private String proxyHost;

    @Value("${vtag.proxy.port}")
    private int proxyPort;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        configProxy();
        var app = FirebaseApp.initializeApp(FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(new ClassPathResource("firebase-token.json").getInputStream()))
            // .setDatabaseUrl("https://vtag-39bef-default-rtdb.firebaseio.com")
            .build());

        return FirebaseMessaging.getInstance(app);
    }

    private void configProxy() {
        if (proxyEnable) {
            log.info("Using proxy {}:{} for firebase", proxyHost, proxyPort);
            System.setProperty("com.google.api.client.should_use_proxy", "true");
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", String.valueOf(proxyPort));
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", String.valueOf(proxyPort));
        }
    }
}
