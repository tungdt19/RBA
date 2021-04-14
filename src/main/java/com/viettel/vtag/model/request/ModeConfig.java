package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.viettel.vtag.utils.TimeDeserializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;

@Data
@Accessors(fluent = true)
public class ModeConfig {

    @JsonProperty("start_time")
    @JsonSerialize(using = TimeSerializer.class)
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime start;

    @JsonProperty("end_time")
    @JsonSerialize(using = TimeSerializer.class)
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime end;

    @JsonProperty
    private int cycle;

    public static ModeConfig of(int start, int end, int cycle) {
        return new ModeConfig().start(LocalTime.of(start, 0)).end(LocalTime.of(end, 0)).cycle(cycle);
    }
}
