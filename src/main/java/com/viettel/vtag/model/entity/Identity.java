package com.viettel.vtag.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Identity {
    private String id;
    private String name;
}
