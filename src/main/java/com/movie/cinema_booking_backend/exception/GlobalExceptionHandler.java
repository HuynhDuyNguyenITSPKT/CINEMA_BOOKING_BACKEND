package com.movie.cinema_booking_backend.exception;

import com.movie.cinema_booking_backend.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — Xử lý tập trung tất cả exception của hệ thống.
 *
 * <p>SOLID — Single Responsibility: Tập trung toàn bộ exception mapping tại đây.
 * <p>Dùng HTTP status từ {@link ErrorCode} để đảm bảo đúng semantics REST.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý các AppException — lỗi business logic có mã lỗi cụ thể.
     * Trả về HTTP status tương ứng với ErrorCode (404, 403, 401, v.v.),
     * không hardcode 400.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("[AppException] code={}, message={}", errorCode.getCode(), errorCode.getMessage());

        ApiResponse<String> response = new ApiResponse.Builder<String>()
                .success(false)
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /**
     * Xử lý lỗi validation từ @Valid — tổng hợp tất cả field errors thành 1 message.
     * Trả về 400 Bad Request với danh sách các field bị lỗi.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("[ValidationError] {}", message);

        ApiResponse<String> response = new ApiResponse.Builder<String>()
                .success(false)
                .message(message)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Xử lý lỗi khi body request không đọc được (sai format JSON, enum sai, v.v.).
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<String>> handleUnreadableMessage(Exception ex) {
        log.warn("[BadRequest] {}", ex.getMessage());

        ApiResponse<String> response = new ApiResponse.Builder<String>()
                .success(false)
                .message("Dữ liệu yêu cầu không hợp lệ hoặc sai định dạng")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Xử lý lỗi phân quyền (403).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("[AccessDenied] {}", ex.getMessage());

        ApiResponse<String> response = new ApiResponse.Builder<String>()
                .success(false)
                .message(ErrorCode.ACCESS_DENIED.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Bắt tất cả exception chưa được handle — fallback cuối cùng.
     * Tránh lộ stack trace ra ngoài client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        log.error("[UnhandledException] {}", ex.getMessage(), ex);

        ApiResponse<String> response = new ApiResponse.Builder<String>()
                .success(false)
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
