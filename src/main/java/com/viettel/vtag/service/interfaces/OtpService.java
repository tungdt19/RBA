package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.OTP;
import com.viettel.vtag.model.request.OtpRequest;

import java.util.Locale;

public interface OtpService {

    OTP generateResetOtp(OtpRequest request);

    OTP generateRegisterOtp(OtpRequest request);

    void sendOtp(OtpRequest request, OTP otp, Locale locale);
}
