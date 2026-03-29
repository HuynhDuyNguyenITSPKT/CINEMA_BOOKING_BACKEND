package com.movie.cinema_booking_backend.service.auth.builder;

public interface TokenDescriptorBuilder {
    void reset(String username, String scope);
    void buildDurationSeconds();
    void buildType();
    void buildToken();
    TokenDescriptor getResult();
}
