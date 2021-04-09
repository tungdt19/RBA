package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(fluent = true)
public class FirmwareUpdateMessage {

    @JsonProperty("Type")
    private final String type = "MFO";

    @JsonProperty("Host")
    private String host;

    @JsonProperty("AppName")
    private String appName;

    @JsonProperty("Ver")
    private String version;

    private Map<String, Object> properties = new HashMap<>();

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }
}
