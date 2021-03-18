package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class TokenRequest {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}
