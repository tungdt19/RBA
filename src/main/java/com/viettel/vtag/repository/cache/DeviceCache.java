package com.viettel.vtag.repository.cache;

import com.viettel.vtag.model.entity.Device;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeviceCache extends ConcurrentHashMap<UUID, Device> { }
