package com.viettel.vtag.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Device {

    private int id;
    private String name;
    private String imei;
    private String platformId;
}
