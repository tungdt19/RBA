package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Identity {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;
}
