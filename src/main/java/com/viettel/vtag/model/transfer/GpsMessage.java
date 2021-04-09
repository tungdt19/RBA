package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettel.vtag.model.ILocation;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class GpsMessage implements ILocation {

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Conn")
    private String connection;

    @JsonProperty("Ver")
    private String version;

    @JsonProperty("Lat")
    private double latitude;

    @JsonProperty("Lon")
    private double longitude;

    @JsonProperty("TS")
    private String timestamp;
}
