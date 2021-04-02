package com.viettel.vtag.repository.impl;

import com.viettel.vtag.model.entity.Location;
import com.viettel.vtag.model.request.LocationHistoryRequest;
import com.viettel.vtag.repository.interfaces.LocationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LocationHistoryRepositoryImpl implements LocationHistoryRepository {

    private final JdbcTemplate jdbc;

    @Override
    public int save(Location location) {
        var sql = "";
        return jdbc.update(sql);
    }

    @Override
    public List<Location> fetch(LocationHistoryRequest request) {
        var sql = "";
        return jdbc.query(sql, new Object[]{}, (rs, num) -> {
            return new Location();
        });
    }
}
