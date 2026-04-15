package com.movie.cinema_booking_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SeatLockProperties {

    @Value("${app.seat-lock.ttl-minutes:10}")
    private long ttlMinutes;

    public long getTtlMinutes() {
        return ttlMinutes;
    }

    public Duration getTtlDuration() {
        long safeMinutes = ttlMinutes <= 0 ? 10 : ttlMinutes;
        return Duration.ofMinutes(safeMinutes);
    }
}

