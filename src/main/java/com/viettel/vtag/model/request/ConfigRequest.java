package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.viettel.vtag.utils.ConfigRequestSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;
import java.util.List;

@Data
@Accessors(fluent = true)
@JsonSerialize(using = ConfigRequestSerializer.class)
public class ConfigRequest {

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
        private LocalTime start;

        @JsonProperty("end_time")
        private LocalTime end;

        @JsonProperty
        private int cycle;
    }
}
