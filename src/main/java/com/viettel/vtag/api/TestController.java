package com.viettel.vtag.api;

import com.viettel.vtag.model.entity.PlatformData;
import com.viettel.vtag.service.impl.DeviceServiceImpl;
import com.viettel.vtag.service.interfaces.CommunicationService;
import com.viettel.vtag.service.interfaces.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final CommunicationService communicationService;
    private final DeviceService deviceService;

    @PostMapping("/sms")
    public void sendSms(@RequestParam String recipient, @RequestParam String content) {
        communicationService.sendSms(recipient, content);
    }

    @PostMapping("/convert")
    public Mono<ClientResponse> getInfo(@RequestBody PlatformData data) {
        return deviceService.convert(data);
    }
}
