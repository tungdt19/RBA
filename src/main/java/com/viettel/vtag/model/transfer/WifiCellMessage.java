package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.viettel.vtag.utils.CellIdSerializer;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Data
@Slf4j
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonSerialize(using = CellIdSerializer.class)
public class WifiCellMessage {

    @JsonIgnore
    private String token;

    @JsonIgnore
    private UUID deviceId;

    @JsonProperty("Type")
    private String type;

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

    public WifiCellMessage token(String token) {
        this.token = token;
        log.info("set token {}", token);
        return this;
    }

    @Override
    public String toString() {
        return "{" + token + ", id=" + deviceId + ", type=" + type + ", con='" + connection + ", cells=" + cells
            + ", aps=" + aps + '}';
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

        @Override
        public String toString() {
            return "{" + "cid=" + cid + ", lac=" + lac + ", mcc=" + mcc + ", mnc=" + mnc + '}';
        }
    }

    @Data
    @Accessors(fluent = true)
    public static class AP {

        @JsonProperty("MAC")
        private String mac;

        @JsonProperty("SS")
        private int ss;

        @Override
        public String toString() {
            return "{" + ss + ", " + mac + '}';
        }
    }
}
