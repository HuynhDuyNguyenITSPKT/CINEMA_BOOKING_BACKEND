package com.movie.cinema_booking_backend.service;

public interface IEmailService {
    void sendOtpEmail(String to, String otp);
}