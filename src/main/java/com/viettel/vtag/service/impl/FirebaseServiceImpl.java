package com.viettel.vtag.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
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

    @Override
    public void message(List<String> topicTokens, Map<String, String> data) {
        try {
            var message = MulticastMessage.builder().putAllData(data).addAllTokens(topicTokens).build();
            var response = FirebaseMessaging.getInstance().sendMulticast(message);
            if (response.getFailureCount() <= 0) {return;}
            var responses = response.getResponses();

            var failedTokens = IntStream.range(0, responses.size())
                .filter(i -> !responses.get(i).isSuccessful())
                .mapToObj(topicTokens::get)
                .collect(Collectors.toCollection(ArrayList::new));

            System.out.println("List of tokens that caused failures: " + failedTokens);
        } catch (FirebaseMessagingException e) {
            log.error("Couldn't send message to user: {}", e.getMessage());
        }
    }
}
