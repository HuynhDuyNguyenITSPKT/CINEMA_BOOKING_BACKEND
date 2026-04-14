package com.movie.cinema_booking_backend.service.bookingticket.observer;

import java.math.BigDecimal;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════════
 *  DESIGN PATTERN: OBSERVER — Event DTO
 * ════════════════════════════════════════════════════════════
 * DTO chứa lượng dữ liệu vừa đủ (State Data) để các Observer có thể
 * lấy ra (PULL) và xử lý hậu kiểm (gửi mail trống ghế, v.v.).
 */
public record BookingSuccessEvent(
        String bookingId,
        String userEmail,
        String movieName,
        String showtimeId,
        List<String> seatIds,
        List<String> ticketIds,
        BigDecimal grandTotal,
        String promotionCode
) {
}
