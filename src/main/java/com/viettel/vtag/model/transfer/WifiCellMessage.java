package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.viettel.vtag.utils.CellIdSerializer;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
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

    @JsonProperty("Bat")
    private int battery;

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

    public static WifiCellMessage fromBinary(byte[] bytes) {
        var payload = new BinaryPayload(bytes);
        var headerType = payload.get();
        var type = (headerType >> 4) & 0xFF;
        var connection = (headerType) & 0x0F;
        var battery = payload.buffer.get();

        System.out.format("type %d; connection %d; battery %d\n", type, connection, battery);

        var count = payload.buffer.get();
        var cellCount = (count >> 4) & 0xFF;
        var wifiCount = (count) & 0x0F;

        for (var i = 0; i < cellCount; i++) {
            var cell = payload.buffer.getInt();
            var lac = payload.buffer.getShort();

            var s1 = payload.buffer.get();
            var s2 = payload.buffer.get();
            var s3 = payload.buffer.get();
            var mnc = (s1 << 4) | (s2 >> 4);
            var mcc = ((s2 & 0x0F) << 8) | (s3 & 0xFF);
            var sig = payload.buffer.get() & 0xFF;

            System.out.format("cell %02X; lac %02X; mnc %02X; mcc %02X; sig -%02X\n", cell, lac, mnc, mcc, sig);
        }

        for (var i = 0; i < wifiCount; i++) {
            System.out.format("MAC: %s; SS: -%d\n", payload.getMAC(), payload.buffer.get());
        }
        return new WifiCellMessage();
    }

    @Override
    public String toString() {
        return "{" + deviceId + ", " + type + ", " + connection + ", " + cells + ", " + aps + '}';
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
