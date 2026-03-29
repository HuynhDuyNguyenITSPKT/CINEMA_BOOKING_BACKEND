package com.movie.cinema_booking_backend.service.auth.builder;

import com.movie.cinema_booking_backend.service.auth.JwtTokenService;

public class AccessTokenDescriptorBuilder implements TokenDescriptorBuilder {

    private final JwtTokenService jwtTokenService;
    private String username;
    private String scope;
    private long durationSeconds;
    private String type;
    private String token;

    public AccessTokenDescriptorBuilder(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void reset(String username, String scope) {
        this.username = username;
        this.scope = scope;
        this.durationSeconds = 0L;
        this.type = null;
        this.token = null;
    }

    @Override
    public void buildDurationSeconds() {
        this.durationSeconds = 3600L;
    }

    @Override
    public void buildType() {
        this.type = "ACCESS";
    }

    @Override
    public void buildToken() {
        this.token = jwtTokenService.generateToken(
                new TokenDescriptor.Builder()
                        .username(username)
                        .scope(scope)
                        .durationSeconds(durationSeconds)
                        .type(type)
                        .build()
        );
    }

    @Override
    public TokenDescriptor getResult() {
        return new TokenDescriptor.Builder()
                .username(username)
                .scope(scope)
                .durationSeconds(durationSeconds)
                .type(type)
                .token(token)
                .build();
    }
}
