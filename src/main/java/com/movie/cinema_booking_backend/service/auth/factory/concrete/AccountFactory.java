package com.movie.cinema_booking_backend.service.auth.factory.concrete;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.PendingRegistration;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.enums.Role;
import com.movie.cinema_booking_backend.service.auth.factory.AuthEntityAbstractFactory;

public class AccountFactory implements AuthEntityAbstractFactory<Account> {

    private final PendingRegistration pending;
    private final User savedUser;

    public AccountFactory(PendingRegistration pending, User savedUser) {
        this.pending = pending;
        this.savedUser = savedUser;
    }

    @Override
    public Account createEntity() {
        return Account.builder()
                .username(pending.getUsername())
                .password(pending.getPassword())
                .role(Role.USER)
                .user(savedUser)
                .isActive(true)
                .build();
    }
}