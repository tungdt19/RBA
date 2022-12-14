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
    public void serialize(WifiCellMessage value, JsonGenerator json, SerializerProvider serializer) throws IOException {

        json.writeStartObject();

        var deviceId = value.deviceId();
        if (deviceId != null) {
            json.writeObjectField("id", deviceId);
        }

        json.writeObjectField("token", value.token());
        json.writeObjectField("radio", "nb-iot"); // value.connection()
        json.writeObjectField("address", 1);

        var cells = value.cells();
        if (cells.size() > 0) {
            var firstCell = cells.get(0);
            json.writeObjectField("mcc", firstCell.mcc());
            json.writeObjectField("mnc", firstCell.mnc());
        } else {
            log.warn("{}> no cell info, {} wifi", deviceId, value.aps().size());
        }

        json.writeArrayFieldStart("cells");
        for (var cell : cells) {
            json.writeStartObject();
            json.writeObjectField("cid", cell.cid());
            json.writeObjectField("lac", cell.lac());
            // json.writeObjectField("psc", 0);
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
