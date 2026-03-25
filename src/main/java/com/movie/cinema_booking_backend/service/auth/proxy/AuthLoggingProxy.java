package com.movie.cinema_booking_backend.service.auth.proxy;

import java.text.ParseException;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.request.AuthRequest;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.response.UserResponse;

@Service
@Primary
public class AuthLoggingProxy extends AbstractAuthProxy {

    public AuthLoggingProxy(AuthTimingProxy nextProxy) {
        super(nextProxy);
    }

    @Override
    public void register(RegistrationRequest request) {
        System.out.println("[Proxy GhiLog] Đăng ký tài khoản: " + request.getUsername());
        next.register(request);
    }

    @Override
    public void verifyOtp(String email, String otp) {
        System.out.println("[Proxy GhiLog] Đang xác thực OTP cho email: " + email);
        next.verifyOtp(email, otp);
    }

    @Override
    public void resendOtp(String email) {
        System.out.println("[Proxy GhiLog] Đang gửi lại OTP cho email: " + email);
        next.resendOtp(email);
    }

    @Override
    public AuthResponse login(AuthRequest req) {
        System.out.println("[Proxy GhiLog] Đang đăng nhập: " + req.getUsername());
        return next.login(req);
    }

    @Override
    public void logout(String token) throws ParseException {
        System.out.println("[Proxy GhiLog] Đang đăng xuất tài khoản với token: " + token);
        next.logout(token);
    }

    @Override
    public AuthResponse refreshToken(String token) throws Exception {
        System.out.println("[Proxy GhiLog] Đang làm mới token: " + token);
        return next.refreshToken(token);
    }

    @Override
    public UserResponse getCurrentUser(Authentication authentication) {
        System.out.println("[Proxy GhiLog] Đang lấy thông tin người dùng hiện tại: " + authentication.getName());
        return next.getCurrentUser(authentication);
    }
}
