package com.movie.cinema_booking_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.movie.cinema_booking_backend.request.AdminAccountUpdateRequest;
import com.movie.cinema_booking_backend.request.UpdateProfileRequest;
import com.movie.cinema_booking_backend.response.AdminUserAccountResponse;
import com.movie.cinema_booking_backend.response.UserResponse;

public interface IUserService {

    UserResponse updateProfile(Authentication authentication, UpdateProfileRequest request);

    Page<AdminUserAccountResponse> getUsersForAdmin(
            int page,
            int size,
            String keyword,
            String email,
            String phone,
            Boolean status
    );

    AdminUserAccountResponse updateUserAccountByAdmin(Long userId, AdminAccountUpdateRequest request);

}
