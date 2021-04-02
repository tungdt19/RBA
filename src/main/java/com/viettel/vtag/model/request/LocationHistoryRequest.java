package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(fluent = true)
public class LocationHistoryRequest {

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty
    private Timestamp from;

    @JsonProperty
    private Timestamp to;
}
