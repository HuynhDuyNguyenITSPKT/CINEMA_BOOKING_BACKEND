package com.movie.cinema_booking_backend.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                       AccessDeniedException accessDeniedException) throws IOException {
        
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED; 

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = new ApiResponse.Builder<>()
                .success(false)
                .message(errorCode.getMessage())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}