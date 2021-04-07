package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettel.vtag.model.ILocation;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Location implements ILocation {
    // {
    //     "status": "ok", "balance": 4999, "lat": 21.066706, "lon": 105.811164, "accuracy": 2500, "message": "...",
    //     "address": "Phố Vệ Hồ, Xuan La Ward, Tay Ho District, Hanoi, 124224, Vietnam"
    // }

    @JsonProperty
    private String status;

    @JsonProperty
    private int balance;

    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lon")
    private double longitude;

    @JsonProperty
    private int accuracy;

    @JsonProperty
    private String message;

    @JsonProperty
    private String address;
}
