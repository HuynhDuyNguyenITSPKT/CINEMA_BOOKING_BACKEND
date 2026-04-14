package com.movie.cinema_booking_backend.service.auth.proxy;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.request.AuthRequest;
import com.movie.cinema_booking_backend.request.ChangePasswordRequest;
import com.movie.cinema_booking_backend.request.ForgotPasswordRequest;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.request.ResetPasswordRequest;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IAuthService;
import com.movie.cinema_booking_backend.service.impl.AuthServiceImpl;

@Service
@Primary
public class AuthLoggingProxy implements IAuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthLoggingProxy.class);
    private final IAuthService next;

    public AuthLoggingProxy(AuthServiceImpl realSubject) {
        this.next = realSubject;
    }

    @Override
    public void register(RegistrationRequest request) {
        long start = System.nanoTime();
        log.info("[AuthProxy] Đăng ký tài khoản username={} email={}", safe(request.getUsername()), safe(request.getEmail()));
        try {
            next.register(request);
        } finally {
            logDuration("đăng ký tài khoản", start);
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        long start = System.nanoTime();
        log.info("[AuthProxy] Xác thực OTP email={}", safe(email));
        try {
            next.verifyOtp(email, otp);
        } finally {
            logDuration("xác thực OTP", start);
        }
    }

    @Override
    public void resendOtp(String email) {
        long start = System.nanoTime();
        log.info("[AuthProxy] Gửi lại OTP email={}", safe(email));
        try {
            next.resendOtp(email);
        } finally {
            logDuration("gửi lại OTP", start);
        }
    }

    @Override
    public AuthResponse login(String type, Object request) {
        long start = System.nanoTime();
        String normalizedType = normalizeType(type);

        if ("password".equals(normalizedType) && request instanceof AuthRequest authRequest) {
            log.info("[AuthProxy] Đăng nhập type={} username={}", normalizedType, safe(authRequest.getUsername()));
        } else if ("google".equals(normalizedType) && request instanceof String token) {
            validateToken(token);
            log.info("[AuthProxy] Đăng nhập type={} token={}", normalizedType, maskToken(token));
        } else {
            log.info("[AuthProxy] Đăng nhập type={}", safe(normalizedType));
        }

        try {
            return next.login(type, request);
        } finally {
            logDuration("đăng nhập", start);
        }
    }

    @Override
    public void logout(String token) throws ParseException {
        long start = System.nanoTime();
        validateToken(token);
        log.info("[AuthProxy] Đăng xuất token={}", maskToken(token));
        try {
            next.logout(token);
        } finally {
            logDuration("đăng xuất", start);
        }
    }

    @Override
    public AuthResponse refreshToken(String token) throws Exception {
        long start = System.nanoTime();
        validateToken(token);
        log.info("[AuthProxy] Làm mới token={}", maskToken(token));
        try {
            return next.refreshToken(token);
        } finally {
            logDuration("làm mới token", start);
        }
    }

    @Override
    public UserResponse getCurrentUser(Authentication authentication) {
        long start = System.nanoTime();
        ensureAuthenticated(authentication);
        log.info("[AuthProxy] Lấy thông tin người dùng hiện tại username={}", authentication.getName());
        try {
            return next.getCurrentUser(authentication);
        } finally {
            logDuration("lấy thông tin người dùng hiện tại", start);
        }
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        long start = System.nanoTime();
        log.info("[AuthProxy] Quên mật khẩu email={}", safe(request.getEmail()));
        try {
            next.forgotPassword(request);
        } finally {
            logDuration("quên mật khẩu", start);
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        long start = System.nanoTime();
        log.info("[AuthProxy] Đặt lại mật khẩu email={}", safe(request.getEmail()));
        try {
            next.resetPassword(request);
        } finally {
            logDuration("đặt lại mật khẩu", start);
        }
    }

    @Override
    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        long start = System.nanoTime();
        ensureAuthenticated(authentication);
        log.info("[AuthProxy] Đổi mật khẩu username={}", authentication.getName());
        try {
            next.changePassword(authentication, request);
        } finally {
            logDuration("đổi mật khẩu", start);
        }
    }

    private void ensureAuthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private void validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "<rỗng>" : value;
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toLowerCase();
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "<rỗng>";
        }
        if (token.length() <= 16) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 6);
    }

    private void logDuration(String action, long startNano) {
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
        log.info("[AuthProxy] {} hoàn tất trong {} ms", action, elapsedMs);
    }
}
