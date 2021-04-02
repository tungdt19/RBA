package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.OTP;
import com.viettel.vtag.model.request.OtpRequest;
import com.viettel.vtag.repository.interfaces.OtpRepository;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.CommunicationService;
import com.viettel.vtag.service.interfaces.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    @Value("${vtag.otp.length}")
    private int length;

    @Value("${vtag.otp.allowed-chars}")
    private String allowedChars;

    private final MessageSource messageSource;
    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final ThreadPoolTaskScheduler scheduler;
    private final CommunicationService communicationService;

    private SecureRandom secureRandom;

    {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ignored) { }
    }

    @Override
    public OTP generate(OtpRequest request) {
        if (userRepository.findByPhone(request.value()) != null) {
            return null;
        }

        return generateOtp();
    }

    private OTP generateOtp() {
        log.info("allowedChars: {}", allowedChars);
        var characters = allowedChars.toCharArray();
        var randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        var chars = new char[length];
        for (int i = 0, l = randomBytes.length; i < l; i++) {
            chars[i] = characters[(((int) randomBytes[i]) & 0xFF) % characters.length];
        }
        return new OTP().content(new String(chars)).expiredInstant(LocalDateTime.now().plusMinutes(5));
    }

    @Override
    public void sendOtp(OtpRequest request, OTP otp) {
        var message = messageSource.getMessage("message.otp", new Object[] {otp.content(), otp.expiredInstant()},
            Locale.ENGLISH);
        log.info("message {}", message);
        communicationService.send(request, message);
    }

    @PostConstruct
    public void clearOldOtp() {
        scheduler.schedule(otpRepository::clearOldOtp, new PeriodicTrigger(5, TimeUnit.MINUTES));
    }
}
