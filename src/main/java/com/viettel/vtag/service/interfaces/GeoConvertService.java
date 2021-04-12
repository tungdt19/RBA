package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.entity.Location;
import com.viettel.vtag.model.transfer.WifiCellMessage;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GeoConvertService {

    Mono<Location> convert(UUID deviceId, WifiCellMessage json);
}
