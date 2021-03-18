package com.viettel.vtag.service;

import com.viettel.vtag.model.request.OtpRequest;

public interface OtpService {
    default String generate() {
        return generate("1234567890");
    }

    String generate(String allowedChars);

    void sendOtp(OtpRequest request, String otp);
}
