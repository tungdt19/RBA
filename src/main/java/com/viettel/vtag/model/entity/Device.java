package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.vtag.model.ILocation;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Slf4j
@Accessors(fluent = true)
public class Device {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty
    private int id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String imei;

    @JsonProperty("platform_device_id")
    private UUID platformId;

    @JsonProperty
    private int battery;

    @JsonProperty
    private double latitude; // Double

    @JsonProperty
    private double longitude; // Double

    @JsonProperty
    private int accuracy;

    @JsonRawValue
    @JsonProperty("geo-fencing")
    private String geoFencing;

    @JsonProperty("uptime")
    private LocalDateTime uptime;

    @JsonProperty
    private int status;

    @JsonIgnore
    private List<Fence> fences;

    public Device parseGeoFencing(String geo) {
        try {
            fences = MAPPER.readValue(geo, new TypeReference<>() { });
        } catch (JsonProcessingException e) {
            log.info("{}: couldn't parse json: {}", platformId, e.getMessage());
        }
        return this;
    }

    public Device location(ILocation location) {
        latitude = location.latitude();
        longitude = location.longitude();
        return this;
    }
}
