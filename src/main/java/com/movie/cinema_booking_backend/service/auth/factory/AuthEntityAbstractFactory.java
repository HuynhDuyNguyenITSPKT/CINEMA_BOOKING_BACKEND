package com.movie.cinema_booking_backend.service.auth.factory;

public interface AuthEntityAbstractFactory<T> {
    T createEntity();
}