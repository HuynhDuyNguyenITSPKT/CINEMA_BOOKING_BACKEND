package com.movie.cinema_booking_backend.service.bookingticket.singleton;
import java.time.Instant;

public record SeatLockEntry(String userId, Instant expiresAt) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
