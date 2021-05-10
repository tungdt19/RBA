package com.viettel.vtag.service.impl;

import com.google.firebase.messaging.*;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.FenceCheck;
import com.viettel.vtag.repository.interfaces.AdminDeviceRepository;
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

    private final Locale locale;
    private final FirebaseMessaging fcm;
    private final MessageSource messageSource;
    private final AdminDeviceRepository deviceRepository;

    @Override
    public void sos(Device device, ILocation location) {
        var title = messageSource.getMessage("message.sos.title", new Object[] { }, locale);
        var body = messageSource.getMessage("message.sos.body", new Object[] {device.name()}, locale);

        var tokens = deviceRepository.getAllViewerTokens(device.platformId());
        var notification = Notification.builder().setTitle(title).setBody(body).build();
        var data = buildData(device, location, body, "ACTION_SOS");

        message(tokens, notification, data);
    }

    @Override
    public void notifySafeZone(Device device, FenceCheck fenceCheck) {
        var title = messageSource.getMessage("message.fence.title", new Object[] { }, locale);
        var body = messageSource.getMessage(fenceCheck.message(), fenceCheck.args(), locale);

        var notification = Notification.builder().setTitle(title).setBody(body).build();
        var tokens = deviceRepository.getAllViewerTokens(device.platformId());

        message(tokens, notification, buildData(device, fenceCheck.location(), body, "ACTION_SAFE_ZONE"));
    }

    @Override
    public BatchResponse message(List<String> tokens, Notification notification, Map<String, String> data) {
        try {
            if (tokens == null) {
                log.error("couldn't get any user");
                return null;
            }

            if (tokens.isEmpty()) {
                log.error("{}: no watcher", data.get("device_id"));
                return null;
            }

            var response = fcm.sendMulticast(buildMulticastMessage(tokens, notification, data));
            if (response == null) {
                log.error("Couldn't get any FCM response");
                return null;
            }
            log.info("{}: {} -> {}/{}/{}", data.get("device_id"), data.get("action"), response.getSuccessCount(),
                response.getFailureCount(), tokens.size());

            return response.getFailureCount() > 0 ? handleFailResponse(tokens, response) : response;
        } catch (FirebaseMessagingException e) {
            log.error("Couldn't send message to user: {}", e.getMessage());
            return null;
        }
    }

    private BatchResponse handleFailResponse(List<String> tokens, BatchResponse response) {
        var responses = response.getResponses();
        log.info("response {}", responses);
        var failedTokens = IntStream.range(0, responses.size())
            .filter(i -> !responses.get(i).isSuccessful())
            .mapToObj(tokens::get)
            .collect(Collectors.toCollection(ArrayList::new));

        log.error("List of tokens that caused failures: {}", failedTokens);
        return response;
    }

    private MulticastMessage buildMulticastMessage(
        List<String> tokens, Notification notification, Map<String, String> data
    ) {
        return MulticastMessage.builder()
            .putAllData(data)
            .setAndroidConfig(AndroidConfig.builder()
                .setNotification(AndroidNotification.builder().setSound("alert2.mp3").build())
                .setPriority(AndroidConfig.Priority.HIGH)
                .build())
            .setApnsConfig(ApnsConfig.builder()
                .setAps(Aps.builder().setSound("alert2.mp3").build())
                .setFcmOptions(ApnsFcmOptions.builder().build())
                .build())
            .setNotification(notification)
            .addAllTokens(tokens)
            .build();
    }

    private Map<String, String> buildData(Device device, ILocation location, String message, String action) {
        //@formatter:off
        return Map.of(
            "click_action", "FLUTTER_NOTIFICATION_CLICK",
            "device_name", device.name(),
            "device_id", device.platformId().toString(),
            "action", action,
            "message", message,
            "latitude", String.valueOf(location.latitude()),
            "longitude", String.valueOf(location.longitude()));
        //@formatter:on
    }
}
