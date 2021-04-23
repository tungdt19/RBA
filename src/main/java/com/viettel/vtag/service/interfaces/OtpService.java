package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.OTP;
import com.viettel.vtag.model.request.OtpRequest;
import reactor.core.publisher.Mono;

import java.util.Locale;

public interface OtpService {

    Mono<OTP> sendRegisterOtp(OtpRequest request, Locale locale);

    Mono<OTP> sendResetOtp(OtpRequest request, Locale locale);
}
