package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ResetPasswordRequest {

    @JsonProperty("otp")
    private String otp;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("password")
    private String password;
}
