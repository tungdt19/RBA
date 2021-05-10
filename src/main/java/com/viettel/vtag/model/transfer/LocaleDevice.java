package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.viettel.vtag.utils.LocaleDeviceSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public class LocaleDevice {

    @JsonProperty
    private String phone;

    @JsonProperty("id")
    private UUID platformId;

    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lon")
    private double longitude;

    @JsonProperty
    private int accuracy;

    @JsonProperty
    private LocalDateTime time;
}
