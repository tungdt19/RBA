package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.viettel.vtag.utils.CellIdSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonSerialize(using = CellIdSerializer.class)
public class WifiCellMessage {

    @JsonProperty("Type")
    private String type;

    @JsonIgnore
    private String token;

    @JsonProperty("Con")
    private String connection;

    @JsonProperty("Cell")
    private List<Cell> cells;

    @JsonProperty("APs")
    private List<AP> aps;

    @JsonProperty("Ver")
    private String version;

    private Map<String, Object> properties = new HashMap<>();

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }

    @Data
    @Accessors(fluent = true)
    public static class Cell {

        @JsonProperty("CID")
        private int cid;

        @JsonProperty("LAC")
        private int lac;

        @JsonProperty("MCC")
        private int mcc;

        @JsonProperty("MNC")
        private int mnc;

        @JsonProperty("SS")
        private int ss;
    }

    @Data
    @Accessors(fluent = true)
    public static class AP {

        @JsonProperty("MAC")
        private String mac;

        @JsonProperty("SS")
        private int ss;
    }
}
