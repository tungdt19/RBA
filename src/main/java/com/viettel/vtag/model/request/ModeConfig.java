package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.viettel.vtag.utils.TimeDeserializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;

@Data
@Accessors(fluent = true)
public class ModeConfig {

    @JsonProperty("start_time")
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime start = LocalTime.of(8, 0);

    @JsonProperty("end_time")
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime end = LocalTime.of(20, 0);

    @JsonProperty
    private int cycle = 5;
}
