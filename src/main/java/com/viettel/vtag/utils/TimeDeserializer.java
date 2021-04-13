package com.viettel.vtag.utils;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;

import java.time.format.DateTimeFormatter;

public class TimeDeserializer extends LocalTimeDeserializer {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m");

    public TimeDeserializer() {
        super(formatter);
    }
}
