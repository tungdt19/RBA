package com.viettel.vtag.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.Notification;
import com.viettel.vtag.model.entity.Fence;
import com.viettel.vtag.model.entity.Location;
import com.viettel.vtag.model.transfer.WifiCellMessage;
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
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.http.ResponseEntity.*;

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
            return ok(Map.of("updated", updated));
        } catch (Exception e) {
            return status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", String.valueOf(e.getMessage())));
        }
    }

    @PostMapping("/fcm")
    public String fcm(@RequestBody String content, Locale locale) {
        var title = messageSource.getMessage("message.sos.title", new Object[] { }, Locale.ENGLISH);
        var body = messageSource.getMessage("message.sos.body", new Object[] {"content"}, Locale.ENGLISH);
        log.info("locale {} -> title: {}; body: {}", locale, title, body);

        var notification = Notification.builder().setTitle(title).setBody(body).build();
        var response = firebaseService.message(List.of(), notification, Map.of("content", content));
        return MessageFormatter.arrayFormat("{}/{}/{}",
            new Object[] {response.getSuccessCount(), response.getFailureCount(), response.getResponses().size()})
            .getMessage();
    }

    @PostMapping("/query")
    public String query(@RequestBody String content, Locale locale) {
        var sql = "SELECT id, content, geo_length, PG_TYPEOF(content) FROM test_json";
        jdbc.query(sql, (rs, rowNum) -> {
            try {
                var id = rs.getInt("id");
                var cont = rs.getString("content");
                var map = new ObjectMapper().readValue(cont, new TypeReference<List<Fence>>() { });
                var length = rs.getString("geo_length");
                log.info("content {}", map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        });
        return null;
    }

    @PostMapping("/convert")
    public Mono<ResponseEntity<Location>> query(@RequestBody WifiCellMessage message) {
        return geoService.convert(message)
            .flatMap(response -> response.bodyToMono(Location.class))
            .map(ResponseEntity::ok);
    }
}
