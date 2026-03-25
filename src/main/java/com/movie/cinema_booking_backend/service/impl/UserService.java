package com.movie.cinema_booking_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IUserService;

@Service
public class UserService implements IUserService{
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .dateOfBirth(user.getDateOfBirth())
                        .role(user.getAccount().getRole())
                        .build())
                .collect(Collectors.toList());
    }
}
