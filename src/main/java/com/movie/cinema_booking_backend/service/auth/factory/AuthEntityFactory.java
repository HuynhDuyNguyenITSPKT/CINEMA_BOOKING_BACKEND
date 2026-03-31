package com.movie.cinema_booking_backend.service.auth.factory;

public final class AuthEntityFactory {

    private AuthEntityFactory() {
    }

    public static <T> T getEntity(AuthEntityAbstractFactory<T> factory) {
        return factory.createEntity();
    }
}