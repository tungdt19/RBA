package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.OTP;

public interface OtpRepository {

    int save(String phone, OTP otp);

    void clearOldOtp();
}
