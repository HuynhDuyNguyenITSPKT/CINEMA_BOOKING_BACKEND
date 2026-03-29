package com.movie.cinema_booking_backend.service.auth.builder;

import org.springframework.stereotype.Component;

@Component
public class TokenDescriptorDirector {

    private void build(TokenDescriptorBuilder builder, String username, String scope) {
        builder.reset(username, scope);
        builder.buildDurationSeconds();
        builder.buildType();
        builder.buildToken();
    }

    public void makeAccessToken(TokenDescriptorBuilder builder, String username, String scope) {
        build(builder, username, scope);
    }

    public void makeRefreshToken(TokenDescriptorBuilder builder, String username, String scope) {
        build(builder, username, scope);
    }
}
