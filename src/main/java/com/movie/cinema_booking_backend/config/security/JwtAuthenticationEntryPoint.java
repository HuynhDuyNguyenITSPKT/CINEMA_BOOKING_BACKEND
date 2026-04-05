package com.movie.cinema_booking_backend.config.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.response.ApiResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint{

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
    ErrorCode errorCode = resolveErrorCode(authException);
    response.setStatus(errorCode.getStatus().value());
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        ApiResponse<?> apiResponse = new ApiResponse.Builder<>()
                .success(false)
                .message(errorCode.getMessage())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }

    private ErrorCode resolveErrorCode(AuthenticationException authException) {
        if (containsMessage(authException, ErrorCode.ACCOUNT_LOCKED.getMessage())
                || containsMessage(authException, "Tài khoản bị khóa")
                || containsMessage(authException, "Tai khoan bi khoa")) {
            return ErrorCode.ACCOUNT_LOCKED;
        }
        return ErrorCode.UNAUTHENTICATED;
    }

    private boolean containsMessage(Throwable throwable, String expectedMessage) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(expectedMessage)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

}
