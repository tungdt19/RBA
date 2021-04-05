package com.viettel.vtag.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseBody {

    @JsonProperty
    private final int code;

    @JsonProperty
    private final String message;

    @JsonProperty
    private Object data;

}
