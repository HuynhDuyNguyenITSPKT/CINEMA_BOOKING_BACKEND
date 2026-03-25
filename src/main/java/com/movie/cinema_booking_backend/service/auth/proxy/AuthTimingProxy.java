package com.movie.cinema_booking_backend.service.auth.proxy;

import java.text.ParseException;

import com.movie.cinema_booking_backend.service.impl.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.request.AuthRequest;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.response.UserResponse;

@Service("authTimingProxy")
public class AuthTimingProxy extends AbstractAuthProxy {

    public AuthTimingProxy(AuthService realSubject) {
        super(realSubject);
    }

    @Override
    public void register(RegistrationRequest request) {
        long start = System.nanoTime();
        try {
            next.register(request);
        } finally {
            System.out.println("[Proxy Thời Gian] register mất " + (System.nanoTime() - start) + " ns");
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        long start = System.nanoTime();
        try {
            next.verifyOtp(email, otp);
        } finally {
            System.out.println("[Proxy Thời Gian] verifyOtp mất " + (System.nanoTime() - start) + " ns");
        }
    }

    @Override
    public void resendOtp(String email) {
        long start = System.nanoTime();
        try {
            next.resendOtp(email);
        } finally {
            System.out.println("[Proxy Thời Gian] resendOtp mất " + (System.nanoTime() - start) + " ns");
        }
    }

    @Override
    public AuthResponse login(AuthRequest req) {
        long start = System.nanoTime();
        try {
            return next.login(req);
        } finally {
            System.out.println("[Proxy Thời Gian] login mất " + (System.nanoTime() - start) + " ns");
        }
    }

    @Override
    public void logout(String token) throws ParseException {
        long start = System.nanoTime();
        try {
            next.logout(token);
        } finally {
            System.out.println("[Proxy Thời Gian] logout mất " + (System.nanoTime() - start) + " ns");
        }
    }

    @Override
    public AuthResponse refreshToken(String token) throws Exception {
        long start = System.nanoTime();
        try {
            return next.refreshToken(token);
        } finally {
            System.out.println("[Proxy Thời Gian] refreshToken mất " + (System.nanoTime() - start) + " ns");
        }
    }

    @Override
    public UserResponse getCurrentUser(Authentication authentication) {
        long start = System.nanoTime();
        try {
            return next.getCurrentUser(authentication);
        } finally {
            System.out.println("[Proxy Thời Gian] getCurrentUser mất " + (System.nanoTime() - start) + " ns");
        }
    }
}
