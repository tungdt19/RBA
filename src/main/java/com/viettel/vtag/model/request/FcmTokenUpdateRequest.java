package com.viettel.vtag.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class FcmTokenUpdateRequest {

    @JsonProperty("fcm_token")
    private String fcmToken;
}
