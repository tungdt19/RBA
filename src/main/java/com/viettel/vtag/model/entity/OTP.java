package com.viettel.vtag.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class OTP {

    private String content;
    private LocalDateTime expiredInstant;
}
