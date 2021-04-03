package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettel.vtag.model.entity.PlatformDevice;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors
public class PlatformDeviceGroup {

    @JsonProperty
    private int total;

    @JsonProperty
    private int offset;

    @JsonProperty
    private int limit;

    @JsonProperty
    private List<PlatformDevice> devices;
}
