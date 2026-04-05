package com.movie.cinema_booking_backend.exception;

/**
 * AppException - Runtime exception dùng cho business logic errors.
 *
 * Immutable sau khi khởi tạo: errorCode không thể thay đổi.
 * Tuân thủ OOP Encapsulation — không có public setter.
 */
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}