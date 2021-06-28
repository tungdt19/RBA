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

    @JsonProperty("Bat")
    private Integer battery;

    @JsonProperty("T")
    private Long timestamp;

    @JsonProperty("Cell")
    private List<Cell> cells;

    @JsonProperty("APs")
    private List<AP> aps;

    @JsonProperty("Ver")
    private String version;

    private Map<String, Object> properties = new HashMap<>();

    public static WifiCellMessage fromBinary(UUID deviceId, byte[] bytes) {
        var payload = new BinaryPayload(bytes);
        var headerType = payload.get();
        var type = (headerType >> 4) & 0xFF;
        var connection = (headerType) & 0x0F;
        var battery = payload.buffer.get();

        var message = new WifiCellMessage();
        var sb = new StringBuilder();
        message.type(String.valueOf(type)).connection(String.valueOf(connection)).battery((int) battery);
        sb.append(String.format("type %d; connection %d; battery %d\n", type, connection, battery));

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

            sb.append(String.format("cell %02X; lac %02X; mnc %02X; mcc %02X; sig -%02X\n", cell, lac, mnc, mcc, sig));
        }

        var aps = new ArrayList<AP>(cellCount);
        for (var i = 0; i < wifiCount; i++) {
            var mac = payload.getMAC();
            var sig = payload.buffer.get();
            sb.append(String.format("MAC: %s; SS: -%d\n", mac, sig));
            aps.add(new AP().mac(mac).ss(sig));
        }
        message.aps(aps);
        log.info("{} > WFC {}", deviceId, sb);
        return message;
    }

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
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
