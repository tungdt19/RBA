package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
public class ChangeDeviceNameRequest {

    @JsonProperty
    private String name;

    @JsonProperty("platform_id")
    @JsonDeserialize(using = UUIDDeserializer.class)
    private UUID platformId;
}
