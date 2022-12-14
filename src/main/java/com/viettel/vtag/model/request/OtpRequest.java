package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class OtpRequest {

    @JsonProperty
    private String type;

    @JsonProperty
    private String value;
}
