package com.movie.cinema_booking_backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.movie.cinema_booking_backend.request.AdminAccountUpdateRequest;
import com.movie.cinema_booking_backend.request.UpdateProfileRequest;
import com.movie.cinema_booking_backend.response.AdminUserAccountResponse;
import com.movie.cinema_booking_backend.response.UserResponse;

public interface IUserService {

    List<UserResponse> getAllUsers();

    UserResponse updateProfile(Authentication authentication, UpdateProfileRequest request);

    Page<AdminUserAccountResponse> getUsersForAdmin(int page, int size, String keyword);

    AdminUserAccountResponse updateUserAccountByAdmin(Long userId, AdminAccountUpdateRequest request);

}
