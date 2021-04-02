package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.OTP;

public interface OtpRepository {

    int save(OTP otp, String phone);

    void clearOldOtp();
}
