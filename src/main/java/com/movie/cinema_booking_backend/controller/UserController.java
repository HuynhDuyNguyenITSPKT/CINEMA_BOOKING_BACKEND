package com.movie.cinema_booking_backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IUserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return new ApiResponse.Builder<List<UserResponse>>()
                .success(true)
                .message("Users retrieved successfully")
                .data(users)
                .build();
    }
}
