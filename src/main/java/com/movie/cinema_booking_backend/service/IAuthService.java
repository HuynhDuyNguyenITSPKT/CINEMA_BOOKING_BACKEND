package com.movie.cinema_booking_backend.service;

import java.text.ParseException;

import org.springframework.security.core.Authentication;

import com.movie.cinema_booking_backend.request.AuthRequest;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.response.UserResponse;

public interface IAuthService {
    void register(RegistrationRequest request);
    void verifyOtp(String email, String otp);
    void resendOtp(String email);
    AuthResponse login(AuthRequest req);
    void logout(String token) throws ParseException;
    AuthResponse refreshToken(String token) throws Exception;
    UserResponse getCurrentUser(Authentication authentication);
}
