package com.viettel.vtag.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true)
public class Device {

    private int id;
    private String name;
    private String imei;
    private UUID platformId;
    private int battery;
}
