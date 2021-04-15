package com.viettel.vtag.service.interfaces;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.transfer.WifiCellMessage;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GeoService {

    Mono<Location> convert(WifiCellMessage json);

    Mono<Location> convert(UUID deviceId, WifiCellMessage json);

    FenceCheck checkFencing(Device deviceId, ILocation location);
}
