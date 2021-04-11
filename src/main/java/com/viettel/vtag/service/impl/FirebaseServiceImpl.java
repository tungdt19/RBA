package com.viettel.vtag.service.impl;

import com.google.firebase.messaging.*;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseServiceImpl implements FirebaseService {

    private final FirebaseMessaging fcm;
    private final MessageSource messageSource;
    private final UserRepository userRepository;

    @Override
    public void sos(UUID deviceId, ILocation location) {
        //@formatter:off
        var notification= Notification.builder()
            .setTitle(messageSource.getMessage("message.sos.title", new Object[] {}, Locale.ENGLISH))
            .setBody(messageSource.getMessage("message.sos.content", new Object[] {}, Locale.ENGLISH))
            .build();
        var tokens = userRepository.fetchAllViewers(deviceId);
        var data = Map.of(
            "click_action", "FLUTTER_NOTIFICATION_CLICK",
            "device_name", "Thiết bị 2",
            "device_id", deviceId.toString(),
            "action", "ACTION_SOS",
            "latitude", String.valueOf(location.latitude()),
            "longitude", String.valueOf(location.longitude()));
        message(tokens, notification, data);
        //@formatter:off
    }

    @Override
    public BatchResponse message(List<String> tokens, Notification notification, Map<String, String> data) {
        try {
            if (tokens.isEmpty()) {
                tokens = List.of(
                    "en7DxvC6SG-q-gEO3LQTeP:APA91bGZDQvHRMlZf84OsfQDMw658IS2D1tqHNO4u8XRKNssSIK-NAjSwhl_pqKrNik8WgzQY"
                        + "-BSMfXmFaQFCLtP6BH9Y8FC610biJfi2s1gcc2fVrMGfWa6JJEIakdXCNhweMAIiEA6");
            }
            var message = MulticastMessage.builder()
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build())
                // .setApnsConfig(ApnsConfig.builder()..build())
                .setNotification(notification)
                .addAllTokens(tokens)
                .build();
            var response = fcm.sendMulticast(message);
            log.info("tokens {} -> success {}; failure {}", tokens, response.getSuccessCount(), response.getFailureCount());
            if (response.getFailureCount() <= 0) return response;
            var responses = response.getResponses();
            log.info("response {}", responses);
            var failedTokens = IntStream.range(0, responses.size())
                .filter(i -> !responses.get(i).isSuccessful())
                .mapToObj(tokens::get)
                .collect(Collectors.toCollection(ArrayList::new));

            log.error("List of tokens that caused failures: {}", failedTokens);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Couldn't send message to user: {}", e.getMessage());
            return null;
        }
    }
}
