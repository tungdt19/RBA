package com.viettel.vtag.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.viettel.vtag.model.transfer.LocaleDevice;

import java.io.IOException;
import java.util.List;

public class LocaleDeviceSerializer extends JsonSerializer<List<LocaleDevice>> {

    @Override
    public void serialize(
        List<LocaleDevice> devices, JsonGenerator json, SerializerProvider provider
    ) throws IOException {
        json.writeStartObject();

        json.writeObjectField("device_count", devices.size());
        json.writeFieldName("list");
        json.writeStartArray();
        for (var device : devices) {
            json.writeStartObject();
            json.writeObjectField("id", device.platformId());
            json.writeObjectField("lat", device.latitude());
            json.writeObjectField("lon", device.longitude());
            json.writeObjectField("accuracy", device.accuracy());
            json.writeObjectField("time", device.time());
            json.writeEndObject();
        }

        json.writeEndArray();
        json.writeEndObject();
    }
}
