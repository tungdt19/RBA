package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(fluent = true)
public class DeviceConfig {

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Mod")
    private String mode;

    @JsonProperty("MID")
    private long mid;

    @JsonProperty("Per")
    private Period period;

    private Map<String, Object> properties;

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }
}
