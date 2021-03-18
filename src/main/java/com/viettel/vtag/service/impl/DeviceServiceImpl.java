package com.viettel.vtag.service.impl;

import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.AddViewerRequest;
import com.viettel.vtag.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService {

    @Override
    public int add(User user, AddViewerRequest deviceId) {
        return 0;
    }

    @Override
    public String getInfo() {
        //TODO: implement this
        return null;
    }

    @Override
    public String getList() {
        //TODO: implement this
        return null;
    }
}
