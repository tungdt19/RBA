package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class BatteryMessage {

    @JsonProperty("Type") //:"DBAT"
    private String type;

    @JsonProperty("Ver") //:"7.2"
    private String version;

    @JsonProperty("Conn") //:"nbiot"
    private String connection;

    @JsonProperty("Lev") //:100
    private int level;
}
