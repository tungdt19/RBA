package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

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

    @Data
    @Accessors(fluent = true)
    public static class Period {

        @JsonProperty("U")
        private String unit;

        @JsonProperty("V")
        private int value;
    }
}
