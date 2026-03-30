package com.movie.cinema_booking_backend.enums;

/**
 * Trạng thái ghế trong ngữ cảnh xem sơ đồ ghế của một suất chiếu.
 *
 * Phân biệt với TicketStatus:
 *   - SeatStatus: trạng thái của GHẾ (trong phòng / trong suất chiếu cụ thể)
 *   - TicketStatus: trạng thái của VÉ sau khi đặt (PROCESSING→BOOKED→USED)
 *
 * AVAILABLE: Ghế trống, có thể đặt.
 * LOCKED:    Ghế đang bị giữ tạm trong RAM bởi một user khác (Phase 2 SeatLockRegistry).
 *            Không lưu vào DB — chỉ tồn tại trong ConcurrentHashMap với TTL.
 * BOOKED:    Ghế đã có Ticket với TicketStatus = BOOKED trong DB.
 *            Không thể đặt nữa cho suất chiếu này.
 */
public enum SeatStatus {
    AVAILABLE,
    LOCKED,
    BOOKED
}
