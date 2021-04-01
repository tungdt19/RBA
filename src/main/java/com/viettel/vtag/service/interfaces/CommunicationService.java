package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.request.OtpRequest;

public interface CommunicationService {
    void send(OtpRequest request, String content);

    void sendSms(String recipient, String content);

    void sendEmail(String recipient, String content);
}
