package com.viettel.vtag.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class PlatformData {

    @JsonProperty("Conn")
    private String conn;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Cell")
    private List<Cell> cells;

    @JsonProperty("APs")
    private List<AP> aps;

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
