package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Location;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationMessage implements ILocation {

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Conn")
    private String connection;

    @JsonProperty("Bat")
    private int battery;

    @JsonProperty("Ver")
    private String version;

    @JsonProperty("Lat")
    private double latitude;

    @JsonProperty("Lon")
    private double longitude;

    @JsonProperty("TS")
    private String timestamp;

    @JsonProperty
    private Integer accuracy;

    @JsonProperty
    private String message;

    @JsonProperty
    private String address;

    private Map<String, Object> properties = new HashMap<>();

    public static LocationMessage fromLocation(Location location, WifiCellMessage payload) {
        return new LocationMessage().type("C" + payload.type().substring(1))
            .connection(payload.connection())
            .version(payload.version())
            .latitude(location.latitude())
            .longitude(location.longitude())
            .accuracy(location.accuracy())
            .message(location.message())
            .address(location.address());
    }

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }
}
