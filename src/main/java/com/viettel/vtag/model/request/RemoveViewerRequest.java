package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RemoveViewerRequest {

    @JsonProperty("viewer_phone")
    private String viewerPhone;

    @JsonProperty("device_id")
    private String deviceId;
}
