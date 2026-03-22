package com.movie.cinema_booking_backend.service;

import java.text.ParseException;

import com.movie.cinema_booking_backend.request.AuthRequest;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.response.AuthResponse;

public interface IAuthService {
    void register(RegistrationRequest request);
    void verifyOtp(String email, String otp);
    void resendOtp(String email);
    AuthResponse login(AuthRequest req);
    void logout(String token) throws ParseException;
    AuthResponse refreshToken(String token) throws Exception;
}
