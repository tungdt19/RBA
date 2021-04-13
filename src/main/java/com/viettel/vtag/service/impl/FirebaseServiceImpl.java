package com.viettel.vtag.service.impl;

import com.google.firebase.messaging.*;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.Fence;
import com.viettel.vtag.model.entity.FenceCheck;
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

    private static final Locale locale = Locale.forLanguageTag("vi_VN");

    private final FirebaseMessaging fcm;
    private final MessageSource messageSource;
    private final UserRepository userRepository;

    @Override
    public void sos(Device device, ILocation location) {
        var title = messageSource.getMessage("message.sos.title", new Object[] { }, locale);
        var body = messageSource.getMessage("message.sos.body", new Object[] {device.name()}, locale);
        var notification = Notification.builder().setTitle(title).setBody(body).build();
        var tokens = userRepository.fetchAllViewers(device.platformId());
        var data = buildData(device, location, "ACTION_SOS");
        message(tokens, notification, data);
    }

    @Override
    public void notifySafeZone(Device device, FenceCheck fenceCheck) {
        var title = messageSource.getMessage("message.fence.title", new Object[] { }, locale);
        var body = messageSource.getMessage(fenceCheck.message(), fenceCheck.args(), locale);
        var notification = Notification.builder().setTitle(title).setBody(body).build();
        var tokens = userRepository.fetchAllViewers(device.platformId());

        message(tokens, notification, buildData(device, fenceCheck.location(), "ACTION_SAFE_ZONE"));
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
            .setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build())
            // .setApnsConfig(ApnsConfig.builder()
            //     .setAps(Aps.builder().setAlert("sound 2").build())
            //     .setFcmOptions(ApnsFcmOptions.builder().build())
            //     .build())
            .setNotification(notification)
            .addAllTokens(tokens)
            .build();
    }

    private Map<String, String> buildData(Device device, ILocation location, String action) {
        //@formatter:off
        return Map.of(
            "click_action", "FLUTTER_NOTIFICATION_CLICK",
            "device_name", device.name(),
            "device_id", device.platformId().toString(),
            "action", action,
            "latitude", String.valueOf(location.latitude()),
            "longitude", String.valueOf(location.longitude()));
        //@formatter:on
    }
}
