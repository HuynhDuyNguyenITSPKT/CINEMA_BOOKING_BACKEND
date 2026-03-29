package com.movie.cinema_booking_backend.service.bookingticket.singleton;

import java.time.Instant;

/**
 * Biểu diễn một slot lock ghế trong bộ nhớ RAM.
 *
 * Dùng Java record (immutable) — lock entry không cần thay đổi sau khi tạo.
 *
 * @param userId    ID của user đang giữ lock.
 * @param expiresAt Thời điểm lock hết hạn (TTL). Sau thời điểm này, ghế sẽ được evict.
 */
public record SeatLockEntry(String userId, Instant expiresAt) {

    /** @return true nếu lock đã hết hạn tại thời điểm hiện tại. */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
