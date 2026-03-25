package com.movie.cinema_booking_backend.service.auth.builder;

public class RefreshTokenDescriptorBuilder implements TokenDescriptorBuilder {

    private long durationSeconds;
    private String type;

    @Override
    public void reset() {
        this.durationSeconds = 0L;
        this.type = null;
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
    public TokenDescriptor getResult() {
        return new TokenDescriptor.Builder()
                .durationSeconds(durationSeconds)
                .type(type)
                .build();
    }
}
