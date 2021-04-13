package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public class Device {

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
    private double accuracy;

    @JsonRawValue
    @JsonProperty("geo-fencing")
    private String geoFencing;

    @JsonProperty("uptime")
    private LocalDateTime uptime;

    @JsonProperty
    private String status;
}
