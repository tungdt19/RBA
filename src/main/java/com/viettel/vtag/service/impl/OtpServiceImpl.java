package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.OTP;
import com.viettel.vtag.model.request.OtpRequest;
import com.viettel.vtag.repository.interfaces.OtpRepository;
import com.viettel.vtag.repository.interfaces.UserRepository;
import com.viettel.vtag.service.interfaces.CommunicationService;
import com.viettel.vtag.service.interfaces.OtpService;
import com.viettel.vtag.utils.PhoneUtils;
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

    private final MessageSource messageSource;
    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final ThreadPoolTaskScheduler scheduler;
    private final CommunicationService communicationService;

    @Value("${vtag.otp.length}")
    private int length;

    @Value("${vtag.otp.allowed-chars}")
    private String allowedChars;

    private SecureRandom secureRandom;

    {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ignored) { }
    }

    @Override
    public OTP generateRegisterOtp(OtpRequest request) {
        if (request.type().equals("phone")) {
            var phone = PhoneUtils.standardize(request.value());
            request.value(phone);
            if (userRepository.findByPhone(phone) != null) {
                return null;
            }

            var otp = generateOtp();
            var inserted = otpRepository.save(otp, phone);

            if (inserted > 0) return otp;
        }

        throw new RuntimeException("Couldn't create OTP");
    }

    @Override
    public OTP generateResetOtp(OtpRequest request) {
        if (request.type().equals("phone")) {
            var phone = PhoneUtils.standardize(request.value());
            request.value(phone);
            if (userRepository.findByPhone(phone) == null) {
                return null;
            }

            var otp = generateOtp();
            var inserted = otpRepository.save(otp, phone);

            if (inserted > 0) return otp;
        }
        throw new RuntimeException("Couldn't create OTP");
    }

    private OTP generateOtp() {
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
    public void sendOtp(OtpRequest request, OTP otp, Locale locale) {
        log.info("{} -> {}", request, otp);
        var params = new Object[] {otp.content(), otp.expiredInstant()};
        communicationService.send(request, messageSource.getMessage("message.otp", params, locale));
    }

    @PostConstruct
    public void clearOldOtp() {
        scheduler.schedule(otpRepository::clearOldOtp, new PeriodicTrigger(5, TimeUnit.MINUTES));
    }
}
