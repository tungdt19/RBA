package com.viettel.vtag.utils;

import org.springframework.http.server.reactive.ServerHttpRequest;

public class TokenUtils {
    private TokenUtils() {}

    public static String getToken(ServerHttpRequest request) {
        var authorization = request.getHeaders().getFirst("Authorization");
        if (authorization == null) {
            return null;
        }
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : null; // "Bearer ".length
    }
}
