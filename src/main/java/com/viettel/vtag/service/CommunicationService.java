package com.viettel.vtag.service;

public interface CommunicationService {
    void send(String content, String... recipients);
}
