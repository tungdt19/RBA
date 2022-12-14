package com.viettel.vtag.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.viettel.vtag.model.request.PairDeviceRequest;

import java.io.IOException;

public class PairDeviceSerializer extends JsonSerializer<PairDeviceRequest> {

    @Override
    public void serialize(PairDeviceRequest request, JsonGenerator json, SerializerProvider p) throws IOException {
        json.writeStartObject();

        json.writeObjectField("name", "VTAG");

        json.writeFieldName("metadata");
        json.writeStartObject();
        json.writeObjectField("imei", request.platformId());
        json.writeEndObject();

        json.writeEndObject();
    }
}
