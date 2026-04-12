package com.movie.cinema_booking_backend.controller;

import java.text.ParseException;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.movie.cinema_booking_backend.request.AuthRequest;
import com.movie.cinema_booking_backend.request.ChangePasswordRequest;
import com.movie.cinema_booking_backend.request.ForgotPasswordRequest;
import com.movie.cinema_booking_backend.request.GoogleLoginRequest;
import com.movie.cinema_booking_backend.request.OtpRequest;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.request.ResetPasswordRequest;
import com.movie.cinema_booking_backend.request.TokenRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegistrationRequest req) {
        authService.register(req);
        return new ApiResponse.Builder<String>()
                .success(true)
                .message("OTP xác thực đã được gửi đến email của bạn. Vui lòng kiểm tra và xác thực để hoàn tất đăng ký.")
                .data(req.getEmail())
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOtp(@Valid @RequestBody OtpRequest req) {
        authService.verifyOtp(req.getEmail(), req.getOtp());
        return new ApiResponse.Builder<String>()
                .success(true)
                .message("Xác thực thành công! Bạn có thể đăng nhập ngay bây giờ.")
                .data(req.getEmail())
                .build();
    }

    @PostMapping("/resend-otp")
    public ApiResponse<Void> resendOtp(@RequestParam String email) {
        authService.resendOtp(email);
        return new ApiResponse.Builder<Void>()
                .success(true)
                .message("Mã OTP mới đã được gửi.")
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        AuthResponse result = authService.login("password", req);
        return new ApiResponse.Builder<AuthResponse>()
                .success(true)
                .message("Đăng nhập thành công.")
                .data(result)
                .build();
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse result = authService.login("google", request.getTokenId());
        return new ApiResponse.Builder<AuthResponse>()
                .success(true)
                .message("Đăng nhập Google thành công.")
                .data(result)
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return new ApiResponse.Builder<String>()
                .success(true)
                .message("OTP đặt lại mật khẩu đã được gửi.")
                .data(req.getEmail())
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return new ApiResponse.Builder<Void>()
                .success(true)
                .message("Đặt lại mật khẩu thành công.")
                .build();
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(authentication, req);
        return new ApiResponse.Builder<Void>()
                .success(true)
                .message("Đổi mật khẩu thành công.")
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestBody TokenRequest req) throws Exception {
        AuthResponse result = authService.refreshToken(req.getToken());
        return new ApiResponse.Builder<AuthResponse>()
                .success(true)
                .data(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody TokenRequest req) throws ParseException {
        authService.logout(req.getToken());
        return new ApiResponse.Builder<Void>()
                .success(true)
                .message("Đăng xuất thành công.")
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(Authentication authentication) {
        UserResponse result = authService.getCurrentUser(authentication);
        return new ApiResponse.Builder<UserResponse>()
                .success(true)
                .data(result)
                .build();
    }

}
