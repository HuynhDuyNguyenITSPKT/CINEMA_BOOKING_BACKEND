package com.movie.cinema_booking_backend.service.auth.login.strategy;

import com.movie.cinema_booking_backend.entity.Account;

public interface LoginStrategy {
    Account authenticate(Object request);
}
