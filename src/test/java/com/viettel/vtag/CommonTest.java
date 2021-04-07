package com.viettel.vtag;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class CommonTest {

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

    @Test
    public void split_topic() {
        var a = Mono.just("A");

        a.subscribe(s -> {
            try {
                log.info("{}", s + 2);
                Thread.sleep(1000);
                log.info("{}", s + 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        a.subscribe(s -> {
            try {
                log.info("{}", s + 1);
                Thread.sleep(500);
                log.info("{}", s + 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testReactor() {
        Mono.just("false")
            .filter(s -> s.length() > 10)
            .defaultIfEmpty("empty1")
            .flatMap(o -> Mono.just(o + " map"))
            .defaultIfEmpty("empty")
            .subscribe(System.out::println);
    }

    @Data
    public static class Info {
        @JsonAlias({"red"})
        public int r;
    }
}
