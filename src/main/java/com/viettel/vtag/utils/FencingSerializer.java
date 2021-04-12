package com.viettel.vtag.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.viettel.vtag.model.entity.Fencing;

import java.io.IOException;

public class FencingSerializer extends JsonSerializer<Fencing> {

    @Override
    public void serialize(Fencing o, JsonGenerator json, SerializerProvider Provider) throws IOException {
        json.writeStartObject();

        json.writeFieldName(o.name());
        json.writeStartObject();
        json.writeObjectField("lat", o.latitude());
        json.writeObjectField("lon", o.longitude());
        json.writeObjectField("radius", o.radius());
        json.writeEndObject();

        json.writeEndObject();
    }
}
