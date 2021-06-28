package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.*;
import com.viettel.vtag.model.ILocation;
import com.viettel.vtag.model.entity.Location;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationMessage implements ILocation {

    @JsonProperty("Type")
    private String type;

    @JsonAlias({"Conn"})
    @JsonProperty("Con")
    private String connection;

    @JsonProperty("Ver")
    private String version;

    @JsonProperty("Lat")
    private double latitude;

    @JsonProperty("Lon")
    private double longitude;

    @JsonProperty("Bat")
    private Integer battery;

    @JsonProperty("TS")
    private String time;

    @JsonProperty("T")
    private Long timestamp;

    @JsonProperty
    private Integer accuracy;

    @JsonProperty
    private String message;

    @JsonProperty
    private String address;

    private Map<String, Object> properties = new HashMap<>();

    public static LocationMessage fromLocation(Location location, WifiCellMessage payload) {
        return new LocationMessage().type("C" + payload.type().substring(1))
            .battery(payload.battery())
            .timestamp(payload.timestamp())
            .connection(payload.connection())
            .version(payload.version())
            .latitude(location.latitude())
            .longitude(location.longitude())
            .accuracy(location.accuracy())
            .message(location.message())
            .address(location.address());
    }

    public Long timestamp() {
        if (timestamp == null) {
            try {
                return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"))
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .getEpochSecond();
            } catch (Exception e) {
                return null;
            }
        }
        return timestamp;
    }

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }
}
