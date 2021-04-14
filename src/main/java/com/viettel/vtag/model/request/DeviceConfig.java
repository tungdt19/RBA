package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class DeviceConfig {

    @JsonProperty
    private String id;

    @JsonProperty
    private int mode = 2;

    @JsonProperty
    private int cycle = 10;

    @JsonProperty("points")
    private List<ModeConfig> schedule = List.of(ModeConfig.of(6, 22, 10), ModeConfig.of(22, 6, 60));
}
