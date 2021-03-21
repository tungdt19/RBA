package com.viettel.vtag;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Slf4j
public class FirebaseTest {

    @BeforeAll
    private static void getAccessToken() throws IOException {
        // var googleCredential = GoogleCredential.fromStream(new FileInputStream("service-account.json"))
        //     .createScoped(Arrays.asList(SCOPES));
        // googleCredential.refreshToken();
        // googleCredential.getAccessToken();
    }

    @Test
    public void a() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var info = mapper.readValue("{\"red\":12}", new TypeReference<Info>() { });
        var s = mapper.writeValueAsString(info);
        log.info("s {}", s);
    }

    @Data
    public static class Info {
        @JsonAlias({"red"})
        public int r;
    }
}
