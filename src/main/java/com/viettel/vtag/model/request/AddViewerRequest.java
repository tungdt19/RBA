package com.viettel.vtag.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class AddViewerRequest {
    private String name;
    private String imei;
}
