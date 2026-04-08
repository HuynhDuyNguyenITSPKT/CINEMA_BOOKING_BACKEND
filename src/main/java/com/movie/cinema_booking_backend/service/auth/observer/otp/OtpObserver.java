package com.movie.cinema_booking_backend.service.auth.observer.otp;

public interface OtpObserver {
    void onOtpGenerated(String email, String otp);
}