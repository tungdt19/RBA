package com.viettel.vtag.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.viettel.vtag.model.transfer.WifiCellMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CellIdSerializer extends JsonSerializer<WifiCellMessage> {

    @Override
    public void serialize(
        WifiCellMessage value, JsonGenerator json, SerializerProvider serializers
    ) throws IOException {
        json.writeStartObject();

        json.writeObjectField("token", value.token());
        json.writeObjectField("radio", "nbiot"); // value.connection()
        json.writeObjectField("address", 1);
        json.writeObjectField("mcc", 452);
        json.writeObjectField("mnc", 4);

        json.writeArrayFieldStart("cells");
        for (var cell : value.cells()) {
            json.writeStartObject();
            json.writeObjectField("cid", cell.cid());
            json.writeObjectField("lac", cell.lac());
            json.writeObjectField("psc", 0);
            json.writeEndObject();
        }
        json.writeEndArray();

        json.writeArrayFieldStart("wifi");
        for (var ap : value.aps()) {
            json.writeStartObject();
            json.writeObjectField("bssid", ap.mac());
            json.writeObjectField("signal", ap.ss());
            json.writeEndObject();
        }
        json.writeEndArray();

        json.writeEndObject();
    }
}
