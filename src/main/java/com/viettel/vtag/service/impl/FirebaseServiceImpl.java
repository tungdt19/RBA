package com.viettel.vtag.service.impl;

import com.google.firebase.messaging.*;
import com.viettel.vtag.service.interfaces.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseServiceImpl implements FirebaseService {

    private final FirebaseMessaging fcm;

    @Override
    public void message(List<String> tokens, Notification notification, Map<String, String> data) {
        try {
            if (tokens.isEmpty()) {
                tokens.add(
                    "en7DxvC6SG-q-gEO3LQTeP:APA91bGZDQvHRMlZf84OsfQDMw658IS2D1tqHNO4u8XRKNssSIK-NAjSwhl_pqKrNik8WgzQY"
                        + "-BSMfXmFaQFCLtP6BH9Y8FC610biJfi2s1gcc2fVrMGfWa6JJEIakdXCNhweMAIiEA6");
            }
            var message = MulticastMessage.builder().putAllData(data).addAllTokens(tokens).build();
            var response = fcm.sendMulticast(message);
            if (response.getFailureCount() <= 0) return;
            var responses = response.getResponses();

            var failedTokens = IntStream.range(0, responses.size())
                .filter(i -> !responses.get(i).isSuccessful())
                .mapToObj(tokens::get)
                .collect(Collectors.toCollection(ArrayList::new));

            log.error("List of tokens that caused failures: {}", failedTokens);
        } catch (FirebaseMessagingException e) {
            log.error("Couldn't send message to user: {}", e.getMessage());
        }
    }
}
