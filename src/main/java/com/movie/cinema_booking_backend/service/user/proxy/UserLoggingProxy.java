package com.movie.cinema_booking_backend.service.user.proxy;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.request.AdminAccountUpdateRequest;
import com.movie.cinema_booking_backend.request.UpdateProfileRequest;
import com.movie.cinema_booking_backend.response.AdminUserAccountResponse;
import com.movie.cinema_booking_backend.response.UserResponse;

@Service
@Primary
public class UserLoggingProxy extends AbstractUserProxy {

    private static final Logger log = LoggerFactory.getLogger(UserLoggingProxy.class);

    public UserLoggingProxy(UserLazyLoadProxy nextProxy) {
        super(nextProxy);
    }

    @Override
    public UserResponse updateProfile(Authentication authentication, UpdateProfileRequest request) {
        long start = System.nanoTime();
        try {
            return next.updateProfile(authentication, request);
        } finally {
            logDuration("cập nhật hồ sơ", start);
        }
    }

    @Override
    public Page<AdminUserAccountResponse> getUsersForAdmin(
            int page,
            int size,
            String keyword,
            String email,
            String phone,
            Boolean status
    ) {
        long start = System.nanoTime();
        try {
            return next.getUsersForAdmin(page, size, keyword, email, phone, status);
        } finally {
            logDuration("lấy danh sách người dùng phân trang", start);
        }
    }

    @Override
    public AdminUserAccountResponse updateUserAccountByAdmin(Long userId, AdminAccountUpdateRequest request) {
        long start = System.nanoTime();
        try {
            return next.updateUserAccountByAdmin(userId, request);
        } finally {
            logDuration("cập nhật tài khoản người dùng bởi quản trị viên", start);
        }
    }

    private void logDuration(String action, long startNano) {
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
        log.info("[UserLoggingProxy] {} hoàn tất trong {} ms", action, elapsedMs);
    }
}