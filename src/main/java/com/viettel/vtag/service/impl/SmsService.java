package com.viettel.vtag.service.impl;

import com.viettel.vtag.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@Service("sms-service")
public class SmsService implements CommunicationService {

    private final JdbcTemplate jdbc;

    public SmsService(@Qualifier("sms-jdbc") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void send(String content, String... recipients) {
        var sql = "INSERT INTO sms (phone, content, sent) VALUES (?, ?, ?)";
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, recipients[i]);
                ps.setString(2, content);
                ps.setInt(3, 0);
            }

            @Override
            public int getBatchSize() {
                return recipients.length;
            }
        });
    }
}
