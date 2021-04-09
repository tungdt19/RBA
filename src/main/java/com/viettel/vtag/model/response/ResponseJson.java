package com.viettel.vtag.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseJson {

    @JsonProperty
    private final int code;

    @JsonProperty
    private final String message;

    @JsonRawValue
    private String json;

    public static ResponseJson of(int code, String message) {
        return new ResponseJson(code, message);
    }

    public static ResponseJson of(int code, String message, String data) {
        return new ResponseJson(code, message, data);
    }
}
