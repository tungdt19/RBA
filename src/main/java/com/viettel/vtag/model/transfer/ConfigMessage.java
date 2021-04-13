package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(fluent = true)
public class ConfigMessage {

    @JsonProperty("Type") // "DCF"
    private String type;

    @JsonProperty("Ver") // "7.2"
    private String version;

    @JsonProperty("Con") // "nbiot"
    private String connection;

    @JsonProperty("MBC") // {"Per":{"V":600,"U":"s"},"Mod":1,"Thre":20}
    private PeriodConfig MBC;

    @JsonProperty("MMC") // {"Per":{"V":2,"U":"m"},"Mod":0}
    private PeriodConfig MMC;

    private Map<String, Object> properties = new HashMap<>();

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }

    @Data
    @Accessors(fluent = true)
    public static class PeriodConfig {

        @JsonProperty("Per")
        private Period period;

        @JsonProperty("Mod")
        private int mode;

        @JsonProperty("Thre")
        private int threshold;
    }
}
