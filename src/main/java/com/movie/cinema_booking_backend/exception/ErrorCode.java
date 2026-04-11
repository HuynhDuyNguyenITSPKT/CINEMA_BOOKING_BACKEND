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
 *   <li>2xxx — Movie / Genre / Showtime / Auditorium</li>
 *   <li>3xxx — Promotion / Extra Service</li>
 *   <li>4xxx — Payment / Booking / Ticket</li>
 *   <li>9xxx — Generic / Uncategorized</li>
 * </ul>
 */
@Getter
public enum ErrorCode {

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
    OAUTH2_CODE_INVALID(1024,               "Mã xác thực OAuth2 không hợp lệ",           HttpStatus.BAD_REQUEST),
    OAUTH2_CODE_EXPIRED(1025,               "Mã xác thực OAuth2 đã hết hạn",             HttpStatus.BAD_REQUEST),

    // ── Movie ─────────────────────────────────────────────────────────
    MOVIE_NOT_FOUND(2001,       "Phim không tồn tại",      HttpStatus.NOT_FOUND),
    MOVIE_TITLE_EXISTS(2002,    "Tên phim đã tồn tại",     HttpStatus.BAD_REQUEST),
    INVALID_MOVIE_STATUS(2003,  "Trạng thái phim không hợp lệ. Chỉ hỗ trợ NOW_SHOWING hoặc COMING_SOON", HttpStatus.BAD_REQUEST),

    // ── Genre ─────────────────────────────────────────────────────────
    GENRE_NOT_FOUND(2010,   "Thể loại không tồn tại",  HttpStatus.NOT_FOUND),
    GENRE_EXISTS(2011,      "Thể loại đã tồn tại",     HttpStatus.BAD_REQUEST),

    // ── Showtime & Auditorium ─────────────────────────────────────────
    AUDITORIUM_NOT_FOUND(2020,  "Phòng chiếu không tồn tại",                          HttpStatus.NOT_FOUND),
    AUDITORIUM_NAME_EXISTS(2021, "Tên phòng chiếu đã tồn tại",                        HttpStatus.BAD_REQUEST),
    AUDITORIUM_NOT_ACTIVE(2022, "Phòng chiếu không ở trạng thái hoạt động",           HttpStatus.BAD_REQUEST),
    OVERLAPPING_SHOWTIME(2023,  "Lịch chiếu bị trùng lặp trong cùng phòng chiếu",    HttpStatus.BAD_REQUEST),
    SHOWTIME_NOT_FOUND(2024, "Suất chiếu không tồn tại",                              HttpStatus.NOT_FOUND),
    SEAT_TYPE_NOT_FOUND(2025, "Loại ghế không tồn tại",                               HttpStatus.NOT_FOUND),
    SEAT_TYPE_NAME_EXISTS(2026, "Tên loại ghế đã tồn tại",                            HttpStatus.BAD_REQUEST),
    SEAT_NOT_FOUND(2027, "Ghế không tồn tại",                                         HttpStatus.NOT_FOUND),
    AUDITORIUM_HAS_TICKETS(2028, "Không thể xoá phòng chiếu vì đã có dữ liệu ghế/vé liên quan", HttpStatus.CONFLICT),
    AUDITORIUM_DELETE_CONFLICT(2029, "Không thể xoá phòng chiếu vì đã có dữ liệu suất chiếu liên quan", HttpStatus.CONFLICT),

    // ── Promotion & Extra Service ─────────────────────────────────────
    PROMOTION_NOT_FOUND(3001,           "Khuyến mãi không tồn tại",                        HttpStatus.NOT_FOUND),
    PROMOTION_CODE_EXISTS(3002,         "Mã khuyến mãi đã tồn tại",                        HttpStatus.BAD_REQUEST),
    INVALID_PROMOTION_DATE_RANGE(3003,  "Ngày kết thúc phải sau ngày bắt đầu khuyến mãi", HttpStatus.BAD_REQUEST),
    EXTRA_SERVICE_NOT_FOUND(3010,   "Dịch vụ thêm không tồn tại",      HttpStatus.NOT_FOUND),
    EXTRA_SERVICE_NAME_EXISTS(3011, "Tên dịch vụ thêm đã tồn tại",     HttpStatus.BAD_REQUEST),

    // ── Payment, Booking & Ticket ─────────────────────────────────────
    PAYMENT_GATEWAY_ERROR(4001,     "Chữ ký thanh toán không hợp lệ",  HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_REQUEST(4002,   "Dữ liệu thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_SUCCESS(4003,           "Thanh toán thành công",            HttpStatus.OK),
    SEAT_ALREADY_TAKEN(4010, "Ghế đã được đặt hoặc đang bị khoá bởi người dùng khác", HttpStatus.CONFLICT),
    BOOKING_NOT_FOUND(4011, "Đơn đặt vé không tồn tại", HttpStatus.NOT_FOUND),
    TICKET_NOT_FOUND(4012, "Vé không tồn tại", HttpStatus.NOT_FOUND),
    TICKET_ALREADY_USED(4013, "Vé đã được sử dụng", HttpStatus.CONFLICT),
    TICKET_CANCELLED(4014, "Vé đã bị huỷ", HttpStatus.CONFLICT),
    TICKET_NOT_PAID(4015, "Vé chưa được thanh toán, không thể check-in", HttpStatus.BAD_REQUEST),
    BOOKING_MIN_SEATS_REQUIRED(4016, "Đặt vé nhóm cần ít nhất 5 ghế", HttpStatus.BAD_REQUEST),
    BOOKING_MAX_SEATS_EXCEEDED(4020, "Số ghế vượt quá giới hạn cho phép", HttpStatus.BAD_REQUEST),
    BOOKING_CLOSED_BEFORE_SHOWTIME(4021, "Đã quá hạn đặt vé online (trước 30 phút)", HttpStatus.BAD_REQUEST),
    AGE_RESTRICTION_NOT_MET(4022, "Khách hàng không đủ tuổi xem phim này", HttpStatus.BAD_REQUEST),
    COUPLE_SEATS_MUST_BE_EVEN(4023, "Vé Couple Sweetbox phải được đặt theo cặp (chẵn)", HttpStatus.BAD_REQUEST),
    COUPLE_BOOKING_ONLY_ALLOWS_SWEETBOX(4024, "Luồng Couple chỉ áp dụng cho ghế loại Sweetbox", HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_PAID(4025, "Không thể huỷ vé đã thanh toán thành công", HttpStatus.CONFLICT),
    BOOKING_ALREADY_CANCELLED(4017, "Đơn đặt vé đã bị huỷ", HttpStatus.CONFLICT),
    PAYMENTSUCCESS(4018, "Thanh đã toán thành công rùi", HttpStatus.OK),
    PAYMENT_ALREADY_EXISTS_UNSUCCESS(4019, "Đã thanh toán chưa thành công", HttpStatus.CONFLICT),

    // ── Generic ───────────────────────────────────────────────────────
    INVALID_KEY(9001,               "Khóa không hợp lệ",               HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(9002,           "Dữ liệu yêu cầu không hợp lệ",    HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(9003,          "Dữ liệu đầu vào không hợp lệ",    HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(9004,     "Lỗi hệ thống nội bộ",             HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED_EXCEPTION(9999,   "Lỗi không xác định",               HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}