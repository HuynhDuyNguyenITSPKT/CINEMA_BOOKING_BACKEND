package com.movie.cinema_booking_backend.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request body cho PUT /api/admin/bookings/{id}/update-seats
 *
 * Admin truyền danh sách seatIds mới (sau khi thương lượng với khách đoàn),
 * hệ thống sẽ gọi lại GroupBookingBuilder để tính lại giá với Pipeline
 * và đồng bộ Tickets (JPA orphanRemoval sẽ xóa ghế bỏ ra, INSERT ghế mới vào).
 */
@Getter
@Setter
public class AdminUpdateGroupSeatsRequest {

    /** UUID của Showtime (Suất chiếu). Có thể đổi suất chiếu nếu khách yêu cầu. */
    private String showtimeId;

    /** Danh sách UUID ghế mới đã thống nhất với khách (phải >= 20). */
    private List<String> seatIds;

    /** Ghi chú admin (tùy chọn). */
    private String adminNote;
}
