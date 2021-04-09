package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public class LocationHistoryRequest {

    @JsonProperty("device_id")
    @JsonDeserialize(using = UUIDDeserializer.class)
    private UUID deviceId;

    @JsonProperty
    private LocalDateTime from;

    @JsonProperty
    private LocalDateTime to;
}
