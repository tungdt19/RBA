package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.AddViewerRequest;
import com.viettel.vtag.model.request.RemoveViewerRequest;

import java.util.List;

public interface DeviceService {

    int addViewer(User user, AddViewerRequest deviceId);

    String getInfo();

    List<Device> getList(User user);

    int remove(User user, RemoveViewerRequest detail);
}
