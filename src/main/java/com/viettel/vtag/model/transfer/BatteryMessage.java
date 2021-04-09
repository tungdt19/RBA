package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(fluent = true)
public class BatteryMessage {

    @JsonProperty("Type") //:"DBAT"
    private String type;

    @JsonProperty("Ver") //:"7.2"
    private String version;

    @JsonProperty("Con") //:"nbiot"
    private String connection;

    @JsonProperty("Lev") //:100
    private int level;

    private Map<String, Object> properties;

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }
}
