package com.viettel.vtag;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class CommonTest {

    @BeforeAll
    public static void test() {

    }

    @Test
    public void test_bcrypt() {
        var encoder = new BCryptPasswordEncoder();

        var s = encoder.encode("test");
        log.info(s);
        assertNotNull(s);
        assertTrue(encoder.matches("test", s));
        assertTrue(encoder.matches("test", "$2a$10$JjHwyUDVisZZwqFPPU5I5OFC4JaXnsvXLi/JRKhsdYzhQWbDezl2G"));

        var t = encoder.encode("test");
        log.info(t);
        assertTrue(encoder.matches("test", t));
        assertNotNull(s);
    }

    @Test
    public void test_token() {
        var token = UUID.randomUUID().toString();
        System.out.println(token);
    }

    @Test
    public void test_mockData() {
        var faker = new Faker(new Locale("vi"));
        log.info(faker.name().name());
    }

    @Test
    public void a() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var info = mapper.readValue("{\"red\":12}", Info.class);
        var s = mapper.writeValueAsString(info);
        log.info("s {}", s);
    }

    @Data
    public static class Info {
        @JsonAlias({"red"})
        public int r;
    }
}
