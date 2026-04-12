package com.movie.cinema_booking_backend.service.auth.login.strategy;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.request.AuthRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Account authenticate(Object request) {
        if (!(request instanceof AuthRequest req)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Thiếu thông tin đăng nhập");
        }

        Account acc = accountRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!acc.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(req.getPassword(), acc.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        return acc;
    }
}
