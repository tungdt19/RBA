package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.OTP;
import com.viettel.vtag.repository.interfaces.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OtpRepositoryImpl implements OtpRepository {

    private final JdbcTemplate jdbc;

    @Override
    public int save(OTP otp, String phone) {
        var sql = "INSERT INTO otp(phone, otp, expired_instant) VALUES (?, ?, ?)";
        return jdbc.update(sql, phone, otp.content(), otp.expiredInstant());
    }

    @Override
    public void clearOldOtp() {
        jdbc.update("DELETE FROM otp WHERE NOW() > expired_instant");
    }
}
