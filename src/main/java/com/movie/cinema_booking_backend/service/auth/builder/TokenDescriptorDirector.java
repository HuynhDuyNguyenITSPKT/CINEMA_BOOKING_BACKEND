package com.movie.cinema_booking_backend.service.auth.builder;

import org.springframework.stereotype.Component;

@Component
public class TokenDescriptorDirector {

    private static final String ACCESS = "ACCESS";
    private static final String REFRESH = "REFRESH";

    public TokenDescriptorBuilder changeBuilder(String type) {
        if (ACCESS.equals(type)) {
            return new AccessTokenDescriptorBuilder();
        }
        if (REFRESH.equals(type)) {
            return new RefreshTokenDescriptorBuilder();
        }
        throw new IllegalArgumentException("Unsupported token type: " + type);
    }

    public TokenDescriptor make(String type) {
        TokenDescriptorBuilder builder = changeBuilder(type);
        builder.reset();
        builder.buildDurationSeconds();
        builder.buildType();
        return builder.getResult();
    }

    public TokenDescriptor buildAccessDescriptor() {
        return make(ACCESS);
    }

    public TokenDescriptor buildRefreshDescriptor() {
        return make(REFRESH);
    }
}
