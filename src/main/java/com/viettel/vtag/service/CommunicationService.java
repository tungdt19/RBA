package com.viettel.vtag.service;

import com.viettel.vtag.model.request.OtpRequest;

public interface CommunicationService {
    void send(OtpRequest request, String content);
}
