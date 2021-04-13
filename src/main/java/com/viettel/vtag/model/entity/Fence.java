package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettel.vtag.model.ILocation;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Fence implements ILocation {

    @JsonProperty
    private String name;

    @JsonProperty("lat")
    private Double latitude;

    @JsonProperty("lon")
    private Double longitude;

    @JsonProperty
    private Double radius;

    @JsonIgnore
    private int in;

    @Override
    public Integer accuracy() {
        return null;
    }
}
