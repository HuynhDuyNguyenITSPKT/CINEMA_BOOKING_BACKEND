package com.movie.cinema_booking_backend.service.auth.factory.concrete;

import java.time.LocalDateTime;

import com.movie.cinema_booking_backend.entity.PendingRegistration;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.service.auth.factory.AuthEntityAbstractFactory;

public class PendingRegistrationFactory implements AuthEntityAbstractFactory<PendingRegistration> {

    private final RegistrationRequest request;
    private final String encodedPassword;
    private final String otp;
    private final LocalDateTime now;

    public PendingRegistrationFactory(RegistrationRequest request, String encodedPassword, String otp, LocalDateTime now) {
        this.request = request;
        this.encodedPassword = encodedPassword;
        this.otp = otp;
        this.now = now;
    }

    @Override
    public PendingRegistration createEntity() {
        return PendingRegistration.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .otp(otp)
                .expiryDate(now.plusMinutes(5))
                .otpGeneratedTime(now)
                .build();
    }
}