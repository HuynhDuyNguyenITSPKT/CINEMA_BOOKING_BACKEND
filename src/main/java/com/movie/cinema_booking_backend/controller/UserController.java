package com.movie.cinema_booking_backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.movie.cinema_booking_backend.request.AdminAccountUpdateRequest;
import com.movie.cinema_booking_backend.request.UpdateProfileRequest;
import com.movie.cinema_booking_backend.response.AdminUserAccountResponse;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PutMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse updated = userService.updateProfile(authentication, request);
        return new ApiResponse.Builder<UserResponse>()
                .success(true)
                .message("Cập nhật hồ sơ thành công")
                .data(updated)
                .build();
    }

    @GetMapping("/pageable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaginationResponse<AdminUserAccountResponse>> getUsersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword) {
        var pageResult = userService.getUsersForAdmin(page, size, keyword);
        var pagination = new PaginationResponse.Builder<AdminUserAccountResponse>()
                .currentItems(pageResult.getContent())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .build();

        return new ApiResponse.Builder<PaginationResponse<AdminUserAccountResponse>>()
                .success(true)
                .message("Lấy danh sách người dùng thành công")
                .data(pagination)
                .build();
    }

    @PutMapping("/account/status/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminUserAccountResponse> updateUserAccountByAdmin(
            @PathVariable Long userId,
            @RequestBody AdminAccountUpdateRequest request) {
        AdminUserAccountResponse result = userService.updateUserAccountByAdmin(userId, request);
        return new ApiResponse.Builder<AdminUserAccountResponse>()
                .success(true)
                .message("Cập nhật tài khoản người dùng thành công")
                .data(result)
                .build();
    }
}
