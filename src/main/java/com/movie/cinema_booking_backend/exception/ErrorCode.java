package com.movie.cinema_booking_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_KEY(1001, "Invalid Key", HttpStatus.BAD_REQUEST),
    USER_EXISTS(1002, "User exists", HttpStatus.BAD_REQUEST),
    GENRE_EXISTS (1003, "Genre exists", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    USERNAME_INVALID(1003,"Username must be at {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(2002, "Invalid request", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1005, "Username not found", HttpStatus.NOT_FOUND),
    UNTHENTICATED(1006, "User is not authenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "User do not have permisson", HttpStatus.FORBIDDEN),
    PERMISSION_ALREADY_EXISTS(1008, "Permission already exists", HttpStatus.BAD_REQUEST),
    ROLE_ALREADY_EXISTS(1009, "Role already exists", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1010, "Role not found", HttpStatus.NOT_FOUND),
    EMAIL_EXISTS(1011, "Email already exists", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_FOUND(1011, "Permission not found", HttpStatus.NOT_FOUND),
    DOB_INVALID(1012, "User must be at least {min} years old", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(2003, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TOKEN(1013, "Invalid token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1014, "Token has expired", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(4000, "Input validation error", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(3000, "Incorrect password", HttpStatus.UNAUTHORIZED),
    EMAIL_EXISTED(3001, "Email already existed", HttpStatus.BAD_REQUEST),
    USERNAME_EXISTED(3002, "Username already existed", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(403, "Tài khoản của bạn đã bị khoá.", HttpStatus.FORBIDDEN), 
    PENDING_REGISTRATION_NOT_FOUND(1015, "Pending registration not found", HttpStatus.NOT_FOUND), 
    INVALID_OTP(1016, "Invalid OTP", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1017, "OTP has expired", HttpStatus.BAD_REQUEST), 
    INVALID_PASSWORD(1018, "Invalid password", HttpStatus.BAD_REQUEST), 
    UNAUTHENTICATED(1019, "User is not authenticated", HttpStatus.UNAUTHORIZED), 
    PHONE_EXISTS(1020, "Phone number already exists", HttpStatus.BAD_REQUEST),
    INVALID_DATE_OF_BIRTH(1021, "Invalid date of birth", HttpStatus.BAD_REQUEST), 
    ACCESS_DENIED(403, "Access Denied", HttpStatus.FORBIDDEN), 
    TOKEN_REVOKED(1022, "Token has been revoked", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_FOUND(1023, "Khuyến mãi không tồn tại", HttpStatus.NOT_FOUND),
    PROMOTION_CODE_EXISTS(1024, "Mã khuyến mãi đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_PROMOTION_DATE_RANGE(1025, "Ngày kết thúc khuyến mãi phải sau ngày bắt đầu", HttpStatus.BAD_REQUEST),
    EXTRA_SERVICE_NOT_FOUND(1026, "Dịch vụ thêm không tồn tại", HttpStatus.NOT_FOUND),
    EXTRA_SERVICE_NAME_EXISTS(1027, "Tên dịch vụ thêm đã tồn tại", HttpStatus.BAD_REQUEST);


    private int code;
    private String message;
    private HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}