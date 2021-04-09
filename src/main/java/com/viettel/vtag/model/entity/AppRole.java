package com.viettel.vtag.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class AppRole {

    private int userId;
    private int deviceId;
    private int roleId;
    private String role;
}
