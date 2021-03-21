package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.request.OtpRequest;
import com.viettel.vtag.service.CommunicationService;
import com.viettel.vtag.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);

    @Value("${vtag.otp.length}")
    private int length;

    private SecureRandom secureRandom;

    private final CommunicationService communicationService;
    private final MessageSource messageSource;

    {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            log.error("");
        }
    }

    @Override
    public String generate(String allowedChars) {
        var characters = allowedChars.toCharArray();
        var randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        var chars = new char[length];
        for (int i = 0, l = randomBytes.length; i < l; i++) {
            chars[i] = characters[(((int) randomBytes[i]) & 0xFF) % characters.length];
        }
        return new String(chars);
    }

    @Override
    public void sendOtp(OtpRequest request, String otp) {
        var message = messageSource.getMessage("message.otp", new Object[] {otp}, Locale.ENGLISH);
        log.info("message {}", message);
        communicationService.send(request, message);
    }
}
