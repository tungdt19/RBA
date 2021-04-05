package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.LocationHistory;
import com.viettel.vtag.model.entity.User;
import com.viettel.vtag.model.request.LocationHistoryRequest;

import java.util.List;

public interface LocationHistoryRepository {

    int save(LocationHistory location);

    List<LocationHistory> fetch(User user, LocationHistoryRequest request);
}
