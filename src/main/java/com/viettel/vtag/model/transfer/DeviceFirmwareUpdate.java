package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class DeviceFirmwareUpdate {

    @JsonProperty("Type")
    private final String type = "MFO";

    @JsonProperty("Host")
    private String host;

    @JsonProperty("AppName")
    private String appName;

    @JsonProperty("Ver")
    private String version;
}
