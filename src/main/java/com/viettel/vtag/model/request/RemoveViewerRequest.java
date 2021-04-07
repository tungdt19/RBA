package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RemoveViewerRequest {

    @JsonProperty("viewer_phone")
    private String viewerPhone;

    @JsonProperty("device_id")
    @JsonDeserialize(using = UUIDDeserializer.class)
    private String deviceId;
}
