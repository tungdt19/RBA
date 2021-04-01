package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class DeviceInfo {
    private String id;

    private String name;
    private String groupID;
    private String groupName;
    private String typeID;
    private int status;

    @JsonProperty("template_id")
    private String templateId;
    @JsonProperty("template_id")
    private String typename;
}
