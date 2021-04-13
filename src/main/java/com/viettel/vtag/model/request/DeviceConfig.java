package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
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
    private List<Config> schedule;

    @Data
    @Accessors(fluent = true)
    public static class Config {

        @JsonProperty("start_time")
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        private LocalTime start;

        @JsonProperty("end_time")
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        private LocalTime end;

        @JsonProperty
        private int cycle;
    }
}
