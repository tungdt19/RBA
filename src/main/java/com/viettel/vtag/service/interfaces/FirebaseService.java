package com.viettel.vtag.service.interfaces;

import com.google.firebase.messaging.Notification;

import java.util.List;
import java.util.Map;

public interface FirebaseService {

    void message(List<String> tokens, Notification notification, Map<String, String> data);
}
