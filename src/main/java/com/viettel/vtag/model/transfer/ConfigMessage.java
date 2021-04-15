package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @JsonAlias({"Conn"})
    @JsonProperty("Con") // "nbiot"
    private String connection;

    @JsonProperty("MBC") // {"Per":{"V":600,"U":"s"},"Mod":1,"Thre":20}
    private PeriodConfig MBC;

    @JsonProperty("MMC") // {"Per":{"V":2,"U":"m"},"Mod":0}
    private PeriodConfig MMC;

    @JsonProperty("Day")
    private Mode day;

    @JsonProperty("Night")
    private Mode night;

    private Map<String, Object> properties = new HashMap<>();

    public static int mode(ConfigMessage config) {
        return config.MMC == null ? 0 : config.MMC.mode;
    }

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

    @Data
    @Accessors(fluent = true)
    public static class Mode {

        @JsonProperty("per")
        private int period;

        @JsonProperty("hour")
        private int hour;

        @JsonAlias({"minute"})
        @JsonProperty("min")
        private int minute;
    }
}
