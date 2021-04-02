package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class Location {

    @JsonIgnore
    private int deviceId;

    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lon")
    private double lon;

    @JsonProperty
    private LocalDateTime time;
}
