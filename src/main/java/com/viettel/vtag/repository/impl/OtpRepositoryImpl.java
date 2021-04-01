package com.viettel.vtag.repository.impl;

import com.viettel.vtag.repository.interfaces.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OtpRepositoryImpl implements OtpRepository {

    private final JdbcTemplate jdbc;

    @Override
    public void clearOldOtp() {
        var sql = "DELETE FROM otp WHERE NOW() > expired_instant";
        jdbc.update(sql);
    }
}
