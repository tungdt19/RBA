package com.viettel.vtag.service.interfaces;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.Notification;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Fence;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FirebaseService {

    BatchResponse message(List<String> tokens, Notification notification, Map<String, String> data);

    void sos(UUID uuid, ILocation location);

    void notifySafeZone(UUID deviceId, Fence fence);
}
