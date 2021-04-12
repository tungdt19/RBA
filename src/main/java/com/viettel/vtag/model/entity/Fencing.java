package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.viettel.vtag.utils.FencingSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@JsonSerialize(using = FencingSerializer.class)
public class Fencing {

    @JsonProperty
    private String name;

    @JsonProperty("lat")
    private Double latitude;

    @JsonProperty("lon")
    private Double longitude;

    @JsonProperty
    private Double radius;
}
