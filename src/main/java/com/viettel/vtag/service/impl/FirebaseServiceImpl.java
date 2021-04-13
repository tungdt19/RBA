package com.viettel.vtag.service.impl;

import com.google.firebase.messaging.*;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.Fence;
import com.viettel.vtag.repository.interfaces.DeviceRepository;
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
    private final DeviceRepository deviceRepository;

    @Override
    public void sos(Device device, ILocation location) {
        //@formatter:off
        var notification= Notification.builder()
            .setTitle(messageSource.getMessage("message.sos.title", new Object[] {}, Locale.ENGLISH))
            .setBody(messageSource.getMessage("message.sos.content", new Object[] {}, Locale.ENGLISH))
            .build();
        var tokens = userRepository.fetchAllViewers(device.platformId());
        var data = Map.of(
            "click_action", "FLUTTER_NOTIFICATION_CLICK",
            "device_name", device.name(),
            "device_id", device.toString(),
            "action", "ACTION_SOS",
            "latitude", String.valueOf(location.latitude()),
            "longitude", String.valueOf(location.longitude()));
        message(tokens, notification, data);
        //@formatter:on
    }

    @Override
    public void notifySafeZone(UUID deviceId, Fence fence) {
        //@formatter:off
        var notification= Notification.builder()
            .setTitle(messageSource.getMessage("message.fence.title", new Object[] {}, Locale.ENGLISH))
            .setBody(messageSource.getMessage("message.fence.content", new Object[] {}, Locale.ENGLISH))
            .build();
        var tokens = userRepository.fetchAllViewers(deviceId);
        var device = deviceRepository.find(deviceId);
        var data = Map.of(
            "click_action", "FLUTTER_NOTIFICATION_CLICK",
            "device_name", device.name(),
            "device_id", deviceId.toString(),
            "action", "ACTION_SOS",
            "latitude", String.valueOf(fence.latitude()),
            "longitude", String.valueOf(fence.longitude()));
        message(tokens, notification, data);
        //@formatter:on
    }

    @Override
    public BatchResponse message(List<String> tokens, Notification notification, Map<String, String> data) {
        try {
            if (tokens.isEmpty()) {
                tokens = List.of(
                    "euMOCUPWbURrjXzAR0uh7b:APA91bEEhZCz8nDABGnQ8ar6tybZWMgdDzb2wrfJqRlUGAwa9TMCj3Fk9nKLEgSas"
                        + "-otKgPYExeY9oWkDXTvzAYRL5nJ5TV8Ql8M6zGo2EQSUmXobsULDPhpSfF2YrxGu5nMklsCZW4a",
                    "en7DxvC6SG-q-gEO3LQTeP:APA91bGZDQvHRMlZf84OsfQDMw658IS2D1tqHNO4u8XRKNssSIK-NAjSwhl_pqKrNik8WgzQY"
                        + "-BSMfXmFaQFCLtP6BH9Y8FC610biJfi2s1gcc2fVrMGfWa6JJEIakdXCNhweMAIiEA6");
            }
            var message = MulticastMessage.builder()
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build())
                .setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder().setAlert("sound 2").build())
                    .setFcmOptions(ApnsFcmOptions.builder().build())
                    .build())
                .setNotification(notification)
                .addAllTokens(tokens)
                .build();

            var response = fcm.sendMulticast(message);
            if (response == null) {
                log.error("Couldn't get any FCM response");
                return null;
            }
            log.info("success {}; failure {}", response.getSuccessCount(), response.getFailureCount());

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
