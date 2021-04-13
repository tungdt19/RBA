package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.viettel.vtag.utils.TimeDeserializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;
import java.util.List;

@Data
@Accessors(fluent = true)
public class DeviceConfig {

    @JsonProperty
    private int mode;

    @JsonProperty
    private int cycle;

    @JsonProperty("points")
    private List<ModeConfig> schedule;
}
