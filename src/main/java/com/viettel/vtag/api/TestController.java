package com.viettel.vtag.api;

import com.google.firebase.messaging.Notification;
import com.viettel.vtag.model.response.ObjectResponse;
import com.viettel.vtag.service.interfaces.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@Data
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final JdbcTemplate jdbc;
    private final MqttClient subscriber;
    private final GeoService geoService;
    private final DeviceService deviceService;
    private final FirebaseService firebaseService;
    private final CommunicationService communicationService;
    private final MessageSource messageSource;

    @PostMapping("/sms")
    public void sendSms(@RequestParam String recipient, @RequestParam String content) {
        try {
            communicationService.sendSms(recipient, content);
        } catch (Exception e) {
            log.error("sms error", e);
        }
    }

    @GetMapping("/mqtt/sub")
    public ResponseEntity<Map<String, Object>> subscribe(
        @RequestParam("id") String id, @RequestParam("subtopic") String subtopic
    ) throws MqttException {
        var topic = "messages/" + id + "/" + subtopic;
        var token = subscriber.subscribeWithResponse(topic);
        return ok(Map.of("topics", token.getTopics()));
    }

    @PostMapping("/mqtt/pub")
    public ResponseEntity<Map<String, String>> publish(@RequestParam String topic, @RequestBody String body) {
        try {
            var msg = new MqttMessage(body.getBytes(StandardCharsets.UTF_8));
            subscriber.publish(topic, msg);
            return ok().body(Map.of("msg", "Okie dokie!"));
        } catch (MqttException e) {
            return status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", "Okie dokie!"));
        }
    }

    @PostMapping("/fcm")
    public String fcm(@RequestBody String content, Locale locale) {
        var title = messageSource.getMessage("message.sos.title", new Object[] { }, locale);
        var body = messageSource.getMessage("message.sos.body", new Object[] {content}, locale);
        log.info("locale {} -> title: {}; body: {}", locale, title, body);

        var notification = Notification.builder().setTitle(title).setBody(body).build();
        var response = firebaseService.message(List.of(), notification, Map.of("content", content));
        return MessageFormatter.arrayFormat("test fcm {}/{}/{}",
            new Object[] {response.getSuccessCount(), response.getFailureCount(), response.getResponses().size()})
            .getMessage();
    }

    @GetMapping("/sql")
    public ObjectResponse query(@RequestBody String sql) {
        try {
            var result = jdbc.query(sql, rs -> {
                var metadata = rs.getMetaData();
                var columnCount = metadata.getColumnCount();

                var list = new ArrayList<>();
                while (rs.next()) {
                    var row = new HashMap<String, Object>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metadata.getColumnName(i), rs.getObject(i));
                    }
                    list.add(row);
                }
                return list;
            });

            log.info("SQL\n\t{}\n  -> return {} row(s)", sql.replace("\n", "\n\t"), result.size());
            return ObjectResponse.of(0, "OK!", result);
        } catch (Exception e) {
            log.error("SQL\n\t{}\n  -> {}", sql, e.getMessage());
            return ObjectResponse.of(1, e.getMessage());
        }
    }

    @PostMapping("/sql")
    public ObjectResponse update(@RequestBody String sql) {
        try {
            var updated = jdbc.update(sql);
            log.info("SQL\n\t{}\n  -> updated {} row(s)", sql.replace("\n", "\n\t"), updated);
            return ObjectResponse.of(0, "Updated" + updated + " row(s)!");
        } catch (Exception e) {
            log.error("SQL\n\t{}\n  -> {}", sql, e.getMessage());
            return ObjectResponse.of(1, e.getMessage());
        }
    }
}
