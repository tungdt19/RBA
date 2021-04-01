package com.viettel.vtag.api;

import com.viettel.vtag.service.interfaces.CommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final CommunicationService communicationService;

    @PostMapping("/sms")
    public void sendSms(@RequestParam String recipient, @RequestParam String content) {
        communicationService.sendSms(recipient, content);
    }
}
