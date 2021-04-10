package com.viettel.vtag.api;

import com.google.firebase.messaging.Notification;
import com.viettel.vtag.service.interfaces.CommunicationService;
import com.viettel.vtag.service.interfaces.DeviceService;
import com.viettel.vtag.service.interfaces.FirebaseService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final JdbcTemplate jdbc;
    private final MqttClient subscriber;
    private final DeviceService deviceService;
    private final FirebaseService firebaseService;
    private final CommunicationService communicationService;

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
        return ResponseEntity.ok(Map.of("topics", token.getTopics()));
    }

    @PostMapping("/mqtt/pub")
    public ResponseEntity<Map<String, String>> publish(@RequestParam String topic, @RequestBody String body) {
        try {
            var msg = new MqttMessage(body.getBytes(StandardCharsets.UTF_8));
            subscriber.publish(topic, msg);
            return ResponseEntity.ok().body(Map.of("msg", "Okie dokie!"));
        } catch (MqttException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", "Okie dokie!"));
        }
    }

    // @GetMapping("/json")
    // public ResponseEntity<Map<String, String>> json() {
    // var sql = "select * from test_json where id = 1";
    // var a = jdbc.query(sql, (rs, name) -> {
    //     rs.get
    //     return
    // });
    // }

    @PostMapping("/sql")
    public ResponseEntity<Map<String, Object>> sql(@RequestBody String sql) {
        try {
            var updated = jdbc.update(sql);
            return ResponseEntity.ok(Map.of("updated", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("msg", String.valueOf(e.getMessage())));
        }
    }

    @PostMapping("/fcm")
    public void fcm(@RequestBody String sql) {
        firebaseService.message(List.of(), Notification.builder().build(), Map.of());
    }
}
