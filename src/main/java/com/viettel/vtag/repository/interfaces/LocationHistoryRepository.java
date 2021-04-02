package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.Location;
import com.viettel.vtag.model.request.LocationHistoryRequest;

import java.util.List;

public interface LocationHistoryRepository {

    int save(Location location);

    List<Location> fetch(LocationHistoryRequest request);
}
