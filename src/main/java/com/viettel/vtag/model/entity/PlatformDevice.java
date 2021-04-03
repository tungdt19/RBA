package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
public class PlatformDevice {

    @JsonProperty("id")
    @JsonDeserialize(using = UUIDDeserializer.class)
    private UUID id;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("groupID")
    private String groupId;

    @JsonProperty("groupName")
    private String groupName;

    @JsonProperty("typeID")
    private String typeID;

    @JsonProperty("typename")
    private String typeName;

    @JsonProperty("status")
    private boolean status;
}
