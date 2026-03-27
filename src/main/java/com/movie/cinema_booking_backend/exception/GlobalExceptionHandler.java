package com.movie.cinema_booking_backend.exception;

import com.movie.cinema_booking_backend.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex) {
        ApiResponse<String> apiResponse = new ApiResponse.Builder<String>()
                .success(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(apiResponse);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(Exception ex) {
        ApiResponse<String> apiResponse = new ApiResponse.Builder<String>()
                .success(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(apiResponse);
    }
}
