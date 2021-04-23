package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.request.OtpRequest;
import reactor.core.publisher.Mono;

import java.util.Locale;

public interface OtpService {

    Mono<Boolean> sendRegisterOtp(OtpRequest request, Locale locale);

    Mono<Boolean> sendResetOtp(OtpRequest request, Locale locale);
}
