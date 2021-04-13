package com.viettel.vtag.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.viettel.vtag.model.request.DeviceConfig;
import com.viettel.vtag.model.request.ModeConfig;

import java.io.IOException;

public class DeviceConfigSerializer extends JsonSerializer<DeviceConfig> {

    @Override
    public void serialize(DeviceConfig config, JsonGenerator json, SerializerProvider provider) throws IOException {
        json.writeStartObject();

        json.writeObjectField("Type", "MMC");
        json.writeObjectField("Mod", config.mode());

        json.writeFieldName("Per");
        json.writeStartObject();
        json.writeObjectField("V", config.cycle());
        json.writeObjectField("U", "m");
        json.writeEndObject();

        var schedule = config.schedule();
        if (schedule == null) {
            writeDayNight(json, new ModeConfig(), new ModeConfig());
            json.writeEndObject();
            return;
        }

        var config1 = schedule.get(0);
        var config2 = schedule.get(1);

        if (config1.start().isBefore(config2.start())) {
            writeDayNight(json, config1, config2);
        } else {
            writeDayNight(json, config2, config1);
        }

        json.writeEndObject();
    }

    private static void writeDayNight(JsonGenerator json, ModeConfig day, ModeConfig night) throws IOException {
        writeConfig(json, day, "Day");
        writeConfig(json, night, "Night");
    }

    private static void writeConfig(JsonGenerator json, ModeConfig config, String dn) throws IOException {
        json.writeFieldName(dn);
        json.writeStartObject();
        var start = config.start();
        json.writeObjectField("hour", start.getHour());
        json.writeObjectField("min", start.getMinute());
        json.writeObjectField("per", config.cycle());
        json.writeEndObject();
    }
}
