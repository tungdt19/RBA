package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class PlatformData {

    @JsonProperty
    private int total;

    @JsonProperty
    private int offset;

    @JsonProperty
    private int limit;

    @JsonProperty("messages")
    private List<Datum> data;

    @Data
    @Accessors(fluent = true)
    public static class Datum {

        @JsonProperty
        private String payload;
    }
}
