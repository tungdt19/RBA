package com.viettel.vtag.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class OTP {

    @JsonProperty("otp")
    private String content;

    @JsonProperty("expire")
    private LocalDateTime expiredInstant;
}
