package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class Period {

    @JsonProperty("U")
    private String unit;

    @JsonProperty("V")
    private int value;
}
