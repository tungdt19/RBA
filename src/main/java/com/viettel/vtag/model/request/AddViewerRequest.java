package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
public class AddViewerRequest {

    @JsonProperty
    private String name;

    @JsonProperty
    private String phone;

    @JsonProperty("device_id")
    @JsonDeserialize(using = UUIDDeserializer.class)
    private UUID deviceId;
}
