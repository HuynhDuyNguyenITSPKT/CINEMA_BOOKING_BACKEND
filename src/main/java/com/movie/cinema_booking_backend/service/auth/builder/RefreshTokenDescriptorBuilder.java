package com.movie.cinema_booking_backend.service.auth.builder;

import com.movie.cinema_booking_backend.service.auth.JwtTokenService;

public class RefreshTokenDescriptorBuilder implements TokenDescriptorBuilder {

    private final JwtTokenService jwtTokenService;
    private String username;
    private String scope;
    private long durationSeconds;
    private String type;
    private String token;

    public RefreshTokenDescriptorBuilder(JwtTokenService jwtTokenService) {
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
        this.durationSeconds = 604800L;
    }

    @Override
    public void buildType() {
        this.type = "REFRESH";
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
