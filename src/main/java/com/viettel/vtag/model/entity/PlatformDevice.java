package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PlatformDevice {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private int battery;

    @JsonProperty
    private double latitude;

    @JsonProperty
    private double longitude;

    @JsonProperty
    private String status;

    @JsonProperty
    private String accuracy;

    @JsonProperty("geofencing_status")
    private String geofencingStatus;

    @JsonProperty
    private String uptime;
}
