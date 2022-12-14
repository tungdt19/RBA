package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RegisterRequest {

    @JsonProperty
    private String otp;

    @JsonProperty
    private String phone;

    @JsonProperty
    private String password;
}
