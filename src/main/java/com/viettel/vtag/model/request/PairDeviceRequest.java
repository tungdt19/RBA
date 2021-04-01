package com.viettel.vtag.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PairDeviceRequest {
    private String imei;
}
