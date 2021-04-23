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
public class JsonResponse {

    @JsonProperty
    private final int code;

    @JsonProperty
    private final String message;

    @JsonRawValue
    @JsonProperty("data")
    private String json;

    public static JsonResponse of(int code, String message) {
        return new JsonResponse(code, message);
    }

    public static JsonResponse of(int code, String message, String data) {
        return new JsonResponse(code, message, data);
    }
}
