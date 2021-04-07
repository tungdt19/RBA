package com.viettel.vtag.api;

import com.viettel.vtag.service.interfaces.CommunicationService;
import com.viettel.vtag.service.interfaces.DeviceService;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final MqttClient mqttClient;
    private final CommunicationService communicationService;
    private final DeviceService deviceService;

    @PostMapping("/sms")
    public void sendSms(@RequestParam String recipient, @RequestParam String content) {
        communicationService.sendSms(recipient, content);
    }

    // @PostMapping("/convert")
    // public Mono<ClientResponse> getInfo(@RequestBody DeviceMessage data) {
    //     return deviceService.convert(data);
    // }

    @GetMapping("mqtt")
    public ResponseEntity<Map<String, Object>> subscribe(
        @RequestParam("id") String id, @RequestParam("subtopic") String subtopic
    ) throws MqttException {
        var topic = MessageFormatter.format("messages/{}/{}", id, subtopic).getMessage();
        var token = mqttClient.subscribeWithResponse(topic);
        return ResponseEntity.ok(Map.of("topics", token.getTopics()));
    }
}
