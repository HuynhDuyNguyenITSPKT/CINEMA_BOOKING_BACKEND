package com.movie.cinema_booking_backend.service.auth.login;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.auth.login.strategy.LoginStrategy;

public class LoginContext {

    private LoginStrategy strategy;

    public void setStrategy(LoginStrategy strategy) {
        this.strategy = strategy;
    }

    public Account authenticate(Object request) {
        if (strategy == null) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Login strategy chưa được cấu hình");
        }
        return strategy.authenticate(request);
    }
}
