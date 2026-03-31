package com.movie.cinema_booking_backend.service.auth.factory.concrete;

import java.time.LocalDateTime;

import com.movie.cinema_booking_backend.entity.PendingPasswordReset;
import com.movie.cinema_booking_backend.service.auth.factory.AuthEntityAbstractFactory;

public class PendingPasswordResetFactory implements AuthEntityAbstractFactory<PendingPasswordReset> {

    private final String email;
    private final String otp;
    private final LocalDateTime now;

    public PendingPasswordResetFactory(String email, String otp, LocalDateTime now) {
        this.email = email;
        this.otp = otp;
        this.now = now;
    }

    @Override
    public PendingPasswordReset createEntity() {
        return PendingPasswordReset.builder()
                .email(email)
                .otp(otp)
                .expiryDate(now.plusMinutes(5))
                .otpGeneratedTime(now)
                .build();
    }
}