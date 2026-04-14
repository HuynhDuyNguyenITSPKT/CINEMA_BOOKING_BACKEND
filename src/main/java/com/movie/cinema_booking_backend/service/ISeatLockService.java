package com.movie.cinema_booking_backend.service;

import java.time.Duration;
import java.util.List;

public interface ISeatLockService {
    void validateForBooking(String showtimeId, List<String> seatIds, String userId);
    void lockSeats(String showtimeId, List<String> seatIds, String userId, Duration ttl);
    void unlockSeats(String showtimeId, List<String> seatIds, String userId);
    void forceUnlockSeats(String showtimeId, List<String> seatIds);
}
