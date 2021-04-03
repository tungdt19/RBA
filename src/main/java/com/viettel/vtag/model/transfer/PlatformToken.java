package com.viettel.vtag.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Data
@Component
@Accessors(fluent = true)
public class PlatformToken {
    // {
    //     "access_token": "0ZeB4Ma12o5pkFL71svMzddjg30NGIjhWF18D0SNCTg.Jc2Q1BG_XhFlKz79exxfN5QKHJ1dGGmrWa2HPn60IVw",
    //     "expires_in": 3599,
    //     "scope": "",
    //     "token_type": "bearer"
    // }

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token_type")
    private String type;

    public PlatformToken update(PlatformToken token) {
        accessToken = token.accessToken;
        expiresIn = token.expiresIn;
        scope = token.scope;
        type = token.type;
        return this;
    }
}
