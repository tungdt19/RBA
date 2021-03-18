package com.viettel.vtag.service;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.AddViewerRequest;

public interface DeviceService {

    int add(User user, AddViewerRequest deviceId);

    String getInfo();

    String getList();
}
