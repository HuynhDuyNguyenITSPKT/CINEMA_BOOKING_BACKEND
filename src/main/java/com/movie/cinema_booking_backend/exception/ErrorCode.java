package com.movie.cinema_booking_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode — Tập trung toàn bộ mã lỗi của hệ thống.
 *
 * <p>Mỗi entry gồm:
 * <ul>
 *   <li>{@code code} — mã số lỗi nội bộ (dùng cho client log / debug)</li>
 *   <li>{@code message} — thông báo lỗi trả về client</li>
 *   <li>{@code status} — HTTP status code tương ứng</li>
 * </ul>
 *
 * <p>OOP — Encapsulation: Fields được khai báo {@code final}, không thể thay đổi sau khi khởi tạo.
 * <p>Dải mã lỗi:
 * <ul>
 *   <li>1xxx — Authentication / Authorization / User</li>
 *   <li>2xxx — Movie / Genre / Showtime</li>
 *   <li>3xxx — Promotion / Extra Service</li>
 *   <li>4xxx — Payment</li>
 *   <li>9xxx — Generic / Uncategorized</li>
 * </ul>
 */
@Getter
public enum ErrorCode {
<<<<<<< HEAD

    // ── Authentication & Authorization ──────────────────────────────
    UNAUTHENTICATED(1001,  "Người dùng chưa xác thực",                  HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002,    "Bạn không có quyền thực hiện thao tác này",  HttpStatus.FORBIDDEN),
    ACCESS_DENIED(1003,   "Truy cập bị từ chối",                        HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1004,   "Token không hợp lệ",                         HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1005,   "Token đã hết hạn",                           HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED(1006,   "Token đã bị thu hồi",                        HttpStatus.UNAUTHORIZED),

    // ── User ─────────────────────────────────────────────────────────
    USER_NOT_FOUND(1010,        "Người dùng không tồn tại",             HttpStatus.NOT_FOUND),
    USER_EXISTS(1011,           "Người dùng đã tồn tại",                HttpStatus.BAD_REQUEST),
    EMAIL_EXISTS(1012,          "Email đã được sử dụng",                HttpStatus.BAD_REQUEST),
    PHONE_EXISTS(1013,          "Số điện thoại đã được sử dụng",        HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1014,        "Tài khoản của bạn đã bị khoá",         HttpStatus.FORBIDDEN),
    INCORRECT_PASSWORD(1015,    "Mật khẩu không chính xác",             HttpStatus.UNAUTHORIZED),
    INVALID_PASSWORD(1016,      "Mật khẩu không hợp lệ",               HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1017,      "Tên đăng nhập không hợp lệ",           HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1018,      "Mật khẩu phải có ít nhất 6 ký tự",    HttpStatus.BAD_REQUEST),
    INVALID_DATE_OF_BIRTH(1019, "Ngày sinh không hợp lệ",               HttpStatus.BAD_REQUEST),

    // ── OTP / Registration / Password Reset ──────────────────────────
    INVALID_OTP(1020,                       "Mã OTP không hợp lệ",                        HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1021,                       "Mã OTP đã hết hạn",                          HttpStatus.BAD_REQUEST),
    PENDING_REGISTRATION_NOT_FOUND(1022,    "Yêu cầu đăng ký không tồn tại",             HttpStatus.NOT_FOUND),
    PENDING_RESET_PASSWORD_NOT_FOUND(1023,  "Yêu cầu đặt lại mật khẩu không tồn tại",   HttpStatus.NOT_FOUND),

    // ── Movie ─────────────────────────────────────────────────────────
    MOVIE_NOT_FOUND(2001,       "Phim không tồn tại",      HttpStatus.NOT_FOUND),
    MOVIE_TITLE_EXISTS(2002,    "Tên phim đã tồn tại",     HttpStatus.BAD_REQUEST),

    // ── Genre ─────────────────────────────────────────────────────────
    GENRE_NOT_FOUND(2010,   "Thể loại không tồn tại",  HttpStatus.NOT_FOUND),
    GENRE_EXISTS(2011,      "Thể loại đã tồn tại",     HttpStatus.BAD_REQUEST),

    // ── Showtime ──────────────────────────────────────────────────────
    AUDITORIUM_NOT_FOUND(2020,  "Phòng chiếu không tồn tại",                          HttpStatus.NOT_FOUND),
    OVERLAPPING_SHOWTIME(2021,  "Lịch chiếu bị trùng lặp trong cùng phòng chiếu",    HttpStatus.BAD_REQUEST),

    // ── Promotion ─────────────────────────────────────────────────────
    PROMOTION_NOT_FOUND(3001,           "Khuyến mãi không tồn tại",                        HttpStatus.NOT_FOUND),
    PROMOTION_CODE_EXISTS(3002,         "Mã khuyến mãi đã tồn tại",                        HttpStatus.BAD_REQUEST),
    INVALID_PROMOTION_DATE_RANGE(3003,  "Ngày kết thúc phải sau ngày bắt đầu khuyến mãi", HttpStatus.BAD_REQUEST),

    // ── Extra Service ─────────────────────────────────────────────────
    EXTRA_SERVICE_NOT_FOUND(3010,   "Dịch vụ thêm không tồn tại",      HttpStatus.NOT_FOUND),
    EXTRA_SERVICE_NAME_EXISTS(3011, "Tên dịch vụ thêm đã tồn tại",     HttpStatus.BAD_REQUEST),

    // ── Payment ───────────────────────────────────────────────────────
    PAYMENT_GATEWAY_ERROR(4001,     "Chữ ký thanh toán không hợp lệ",  HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_REQUEST(4002,   "Dữ liệu thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_SUCCESS(4003,           "Thanh toán thành công",            HttpStatus.OK),

    // ── Generic ───────────────────────────────────────────────────────
    INVALID_KEY(9001,               "Khóa không hợp lệ",               HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(9002,           "Dữ liệu yêu cầu không hợp lệ",    HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(9003,          "Dữ liệu đầu vào không hợp lệ",    HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(9004,     "Lỗi hệ thống nội bộ",             HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED_EXCEPTION(9999,   "Lỗi không xác định",               HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus status;
=======
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
    PENDING_RESET_PASSWORD_NOT_FOUND(1023, "Pending reset password not found", HttpStatus.NOT_FOUND),
    PROMOTION_NOT_FOUND(1023, "Khuyến mãi không tồn tại", HttpStatus.NOT_FOUND),
    PROMOTION_CODE_EXISTS(1024, "Mã khuyến mãi đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_PROMOTION_DATE_RANGE(1025, "Ngày kết thúc khuyến mãi phải sau ngày bắt đầu", HttpStatus.BAD_REQUEST),
    EXTRA_SERVICE_NOT_FOUND(1026, "Dịch vụ thêm không tồn tại", HttpStatus.NOT_FOUND),
    EXTRA_SERVICE_NAME_EXISTS(1027, "Tên dịch vụ thêm đã tồn tại", HttpStatus.BAD_REQUEST),

    // ─── Phase 1: Auditorium & SeatType ───────────────────────────────────────
    AUDITORIUM_NOT_FOUND(1028, "Phòng chiếu không tồn tại", HttpStatus.NOT_FOUND),
    AUDITORIUM_NAME_EXISTS(1029, "Tên phòng chiếu đã tồn tại", HttpStatus.BAD_REQUEST),
    AUDITORIUM_NOT_ACTIVE(1030, "Phòng chiếu không ở trạng thái hoạt động", HttpStatus.BAD_REQUEST),
    SEAT_TYPE_NOT_FOUND(1031, "Loại ghế không tồn tại", HttpStatus.NOT_FOUND),
    SEAT_TYPE_NAME_EXISTS(1032, "Tên loại ghế đã tồn tại", HttpStatus.BAD_REQUEST),

    // ─── Phase 2-4: Seat Lock, Booking, Ticket ────────────────────────────────
    SEAT_NOT_FOUND(1033, "Ghế không tồn tại", HttpStatus.NOT_FOUND),
    SEAT_ALREADY_TAKEN(1034, "Ghế đã được đặt hoặc đang bị khoá bởi người dùng khác", HttpStatus.CONFLICT),
    BOOKING_NOT_FOUND(1035, "Đơn đặt vé không tồn tại", HttpStatus.NOT_FOUND),
    TICKET_NOT_FOUND(1036, "Vé không tồn tại", HttpStatus.NOT_FOUND),
    TICKET_ALREADY_USED(1037, "Vé đã được sử dụng", HttpStatus.CONFLICT),
    TICKET_CANCELLED(1038, "Vé đã bị huỷ", HttpStatus.CONFLICT),
    TICKET_NOT_PAID(1039, "Vé chưa được thanh toán, không thể check-in", HttpStatus.BAD_REQUEST),
    SHOWTIME_NOT_FOUND(1040, "Suất chiếu không tồn tại", HttpStatus.NOT_FOUND),
    BOOKING_MIN_SEATS_REQUIRED(1041, "Đặt vé nhóm cần ít nhất 5 ghế", HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_CANCELLED(1042, "Đơn đặt vé đã bị huỷ", HttpStatus.CONFLICT),


    PAYMENT_GATEWAY_ERROR(1028, "Sai chữ ký , lỗi có thể do hash sai", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_REQUEST(1029, "Dữ liệu thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENTSUCCESS(1030, "Thanh đã toán thành công rùi", HttpStatus.OK);
    private int code;
    private String message;
    private HttpStatus status;
>>>>>>> c9612b78620e7daf8fbeb938968fe7dbb583d807

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}