package com.movie.cinema_booking_backend.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for building consistent API responses
 * Follows DRY principle - reduces code duplication in controllers
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponseBuilder {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse.Builder<T>()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse.Builder<T>()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse.Builder<T>()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse.Builder<T>()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse.Builder<T>()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}
