package com.viettel.vtag.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.viettel.vtag.model.entity.PlatformData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CellIdSerializer extends JsonSerializer<PlatformData> {

    @Value("${vtag.unwired.token}")
    private String token;

    @Override
    public void serialize(PlatformData value, JsonGenerator json, SerializerProvider serializers) throws IOException {
        json.writeStartObject();

        json.writeObjectField("token", "284a3628cddb31");
        json.writeObjectField("radio", value.conn());
        json.writeObjectField("address", 1);
        json.writeObjectField("mcc", 452);
        json.writeObjectField("mnc", 4);

        json.writeArrayFieldStart("cell");
        for (var cell : value.cells()) {
            json.writeStartObject();
            json.writeObjectField("cid", cell.cid());
            json.writeObjectField("lac", cell.lac());
            json.writeObjectField("psc", 0);
            json.writeEndObject();
        }
        json.writeEndArray();

        json.writeEndObject();
    }
}
