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
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.PostConstruct;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

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
    public Mono<OTP> sendRegisterOtp(OtpRequest request, Locale locale) {
        return generateOtp(request, phone -> userRepository.findByPhone(phone) == null)
            .doOnNext(otp -> log.info("{} -> {}", request, otp))
            .filter(otp -> {
                var params = new Object[] {otp.content(), otp.expiredInstant()};
                var message = messageSource.getMessage("message.otp.register", params, locale);
                return communicationService.send(request, message) > 0;
            });
    }

    @Override
    public Mono<OTP> sendResetOtp(OtpRequest request, Locale locale) {
        return generateOtp(request, phone -> userRepository.findByPhone(phone) != null)
            .doOnNext(otp -> log.info("{} -> {}", request, otp))
            .filter(otp -> {
                var params = new Object[] {otp.content(), otp.expiredInstant()};
                var message = messageSource.getMessage("message.otp.reset", params, locale);
                return communicationService.send(request, message) > 0;
            });
    }

    private Mono<OTP> generateOtp(OtpRequest request, Predicate<String> checkPhone) {
        return Mono.justOrEmpty(request)
            .filter(req -> req.type().equals("phone"))
            .map(req -> PhoneUtils.standardize(request.value()))
            .filter(checkPhone)
            .zipWith(generateOtp())
            .filter(tuple -> otpRepository.save(tuple.getT1(), tuple.getT2()) > 0)
            .map(Tuple2::getT2)
            .switchIfEmpty(Mono.error(new RuntimeException("Couldn't create OTP")));
    }

    private Mono<OTP> generateOtp() {
        return Mono.fromCallable(() -> {
            var characters = allowedChars.toCharArray();
            var randomBytes = new byte[length];
            secureRandom.nextBytes(randomBytes);
            var chars = new char[length];
            for (int i = 0, l = randomBytes.length; i < l; i++) {
                chars[i] = characters[(((int) randomBytes[i]) & 0xFF) % characters.length];
            }
            return new String(chars);
        }).map(otp -> new OTP().content(otp).expiredInstant(LocalDateTime.now().plusMinutes(5)));
    }

    @PostConstruct
    public void clearOldOtp() {
        scheduler.schedule(otpRepository::clearOldOtp, new PeriodicTrigger(5, TimeUnit.MINUTES));
    }
}
