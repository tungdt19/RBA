package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.request.OtpRequest;
import com.viettel.vtag.service.interfaces.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@Service
public class CommunicationServiceImpl implements CommunicationService {

    private final JdbcTemplate jdbc;

    public CommunicationServiceImpl(@Qualifier("sms-jdbc") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int send(OtpRequest request, String content) {
        switch (request.type()) {
            case "phone":
                return sendSms(request.value(), content);
            case "email":
                return sendEmail(request.value(), content);
        }
        return 0;
    }

    @Override
    public int sendSms(String recipient, String content) {
        var sql = "INSERT INTO sms (phone, content, sent) VALUES (?, ?, ?)";
        return jdbc.update(sql, recipient, content, 0);
    }

    @Override
    public int sendEmail(String recipient, String content) {
        return 0;
    }

    private void sendSms(String[] recipients, String content) {
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
