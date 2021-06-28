package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Accessors(fluent = true)
public class TimeMessage {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd,HH:mm:ss");

    public static String current() {
        return "{\"Type\":\"MTIME\",\"Time\":\"" + LocalDateTime.now().format(formatter) + "\"}";
    }
}
