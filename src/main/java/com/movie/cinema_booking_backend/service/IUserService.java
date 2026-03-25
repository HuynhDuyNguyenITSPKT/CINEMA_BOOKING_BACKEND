package com.movie.cinema_booking_backend.service;

import java.util.List;

import com.movie.cinema_booking_backend.response.UserResponse;

public interface IUserService {

    List<UserResponse> getAllUsers();

}
