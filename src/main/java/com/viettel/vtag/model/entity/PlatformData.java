package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.viettel.vtag.utils.CellIdSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonSerialize(using = CellIdSerializer.class)
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
