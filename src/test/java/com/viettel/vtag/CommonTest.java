package com.viettel.vtag;

import com.github.javafaker.Faker;
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
}
