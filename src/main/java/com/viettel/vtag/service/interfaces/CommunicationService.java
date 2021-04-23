package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.request.OtpRequest;

public interface CommunicationService {

    int send(OtpRequest request, String content);

    int sendSms(String recipient, String content);

    int sendEmail(String recipient, String content);
}
