package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.viettel.vtag.utils.PairDeviceSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@JsonSerialize(using = PairDeviceSerializer.class)
public class PairDeviceRequest {

    @JsonProperty("platform_id")
    private String platformId;
}
