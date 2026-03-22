package com.movie.cinema_booking_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    
    @GetMapping("")
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ApiResponse.Builder<List<User>>()
                .success(true)
                .message("Users retrieved successfully")
                .data(users)
                .build();
    }
}
