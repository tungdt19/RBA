package com.viettel.vtag.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        var app = FirebaseApp.initializeApp(FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(new ClassPathResource("firebase-token.json").getInputStream()))
            // .setDatabaseUrl("https://vtag-39bef-default-rtdb.firebaseio.com")
            .build());
        return FirebaseMessaging.getInstance(app);
    }
}
