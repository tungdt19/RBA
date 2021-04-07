package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.*;
import com.viettel.vtag.model.request.LocationHistoryRequest;

import java.util.List;
import java.util.UUID;

public interface LocationHistoryRepository {

    int save(UUID device, ILocation location);

    List<LocationHistory> fetch(User user, LocationHistoryRequest request);
}
