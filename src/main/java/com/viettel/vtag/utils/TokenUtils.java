package com.viettel.vtag.utils;

import org.springframework.http.server.reactive.ServerHttpRequest;

public class TokenUtils {
    private TokenUtils() {}

    public static String getToken(ServerHttpRequest request) {
        var authorizations = request.getHeaders().get("Authorization");
        if (authorizations == null || authorizations.size() == 0) {
            return null;
        }
        var token = authorizations.get(0);
        return token.startsWith("Bearer ") ? token.substring(7) : null; // "Bearer ".length
    }
}
