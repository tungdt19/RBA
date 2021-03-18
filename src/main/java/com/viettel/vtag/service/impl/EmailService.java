package com.viettel.vtag.service.impl;

import com.viettel.vtag.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
// @Service("email-service")
@RequiredArgsConstructor
public class EmailService implements CommunicationService {

    @Override
    public void send(String content, String... recipients) {

    }
}
