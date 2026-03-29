package com.movie.cinema_booking_backend.enums;

/**
 * Trạng thái hoạt động của phòng chiếu (Auditorium).
 *
 * - ACTIVE:             Phòng đang hoạt động bình thường, có thể tạo Showtime.
 * - UNDER_MAINTENANCE:  Phòng đang bảo trì, tạm thời không thể tạo Showtime.
 * - INACTIVE:           Phòng ngừng hoạt động vĩnh viễn / tạm ngừng dài hạn.
 *
 * Sử dụng trong Phase 4 bởi State Pattern (AuditoriumState) để guard việc tạo Showtime.
 */
public enum AuditoriumStatus {
    ACTIVE,
    UNDER_MAINTENANCE,
    INACTIVE
}
