package com.movie.cinema_booking_backend.service.auth.builder;

public interface TokenDescriptorBuilder {
    void reset();
    void buildDurationSeconds();
    void buildType();
    TokenDescriptor getResult();
}
