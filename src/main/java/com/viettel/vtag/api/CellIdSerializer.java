package com.viettel.vtag.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CellIdSerializer extends JsonSerializer<PlatformData> {

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
