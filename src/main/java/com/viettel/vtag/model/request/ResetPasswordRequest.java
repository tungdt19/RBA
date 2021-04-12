package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ResetPasswordRequest {

    @JsonProperty
    private String otp;

    @JsonProperty
    private String phone;

    @JsonProperty("new_password")
    private String password;
}
