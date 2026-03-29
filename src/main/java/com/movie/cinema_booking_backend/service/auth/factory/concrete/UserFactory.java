package com.movie.cinema_booking_backend.service.auth.factory.concrete;

import com.movie.cinema_booking_backend.entity.PendingRegistration;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.service.auth.factory.AuthEntityAbstractFactory;

public class UserFactory implements AuthEntityAbstractFactory<User> {

    private final PendingRegistration pending;

    public UserFactory(PendingRegistration pending) {
        this.pending = pending;
    }

    @Override
    public User createEntity() {
        return User.builder()
                .fullName(pending.getFullName())
                .email(pending.getEmail())
                .phone(pending.getPhone())
                .dateOfBirth(pending.getDateOfBirth())
                .build();
    }
}