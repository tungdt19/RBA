package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Device;
import com.viettel.vtag.model.entity.Fence;
import com.viettel.vtag.model.entity.Location;
import com.viettel.vtag.model.transfer.WifiCellMessage;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GeoService {

    Mono<Location> convert(UUID deviceId, WifiCellMessage json);

    Mono<Fence> checkFencing(Device deviceId, ILocation location);
}
