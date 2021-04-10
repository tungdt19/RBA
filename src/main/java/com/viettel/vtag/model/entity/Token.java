package com.viettel.vtag.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public class Token {

    private UUID uuid;
    private LocalDateTime expire;

    public static Token generate() {
        return new Token().uuid(UUID.randomUUID()).expire(LocalDateTime.now().plusHours(3));
    }
}
