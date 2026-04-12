package com.movie.cinema_booking_backend.service.auth.login.factory;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.auth.login.strategy.GoogleLoginStrategy;
import com.movie.cinema_booking_backend.service.auth.login.strategy.LoginStrategy;
import com.movie.cinema_booking_backend.service.auth.login.strategy.PasswordLoginStrategy;
import org.springframework.stereotype.Component;

@Component
public class LoginStrategyFactory {

    private final PasswordLoginStrategy passwordStrategy;
    private final GoogleLoginStrategy googleStrategy;

    public LoginStrategyFactory(
            PasswordLoginStrategy passwordStrategy,
            GoogleLoginStrategy googleStrategy
    ) {
        this.passwordStrategy = passwordStrategy;
        this.googleStrategy = googleStrategy;
    }

    public LoginStrategy get(String type) {
        String normalizedType = type == null ? "" : type.trim().toLowerCase();

        if ("password".equals(normalizedType)) {
            return passwordStrategy;
        }

        if ("google".equals(normalizedType)) {
            return googleStrategy;
        }

        throw new AppException(ErrorCode.INVALID_REQUEST, "Phương thức đăng nhập không hỗ trợ");
    }
}
