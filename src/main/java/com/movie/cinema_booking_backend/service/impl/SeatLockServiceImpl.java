package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.ISeatLockService;
import com.movie.cinema_booking_backend.service.bookingticket.singleton.SeatLockRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════
 *  SEAT LOCK SERVICE IMPLEMENTATION
 * ═══════════════════════════════════════════════════════════
 *
 * Xử lý operations lock/unlock/validate seats.
 * KHÔNG phải Proxy, chỉ là service thông thường.
 */
@Service
@RequiredArgsConstructor
public class SeatLockServiceImpl implements ISeatLockService {

    private final SeatLockRegistry lockRegistry = SeatLockRegistry.getInstance();

    @Override
    public void validateForBooking(String showtimeId, List<String> seatIds, String userId) {
        for (String seatId : seatIds) {
            if (lockRegistry.isLockedByOther(showtimeId, seatId, userId)) {
                throw new AppException(ErrorCode.SEAT_ALREADY_TAKEN);
            }
        }
    }

    @Override
    public void lockSeats(String showtimeId, List<String> seatIds, String userId, Duration ttl) {
        System.out.println("[SeatLockService] Locking seats " + seatIds + " for user " + userId);
        lockRegistry.tryLockAll(showtimeId, seatIds, userId, ttl);
        System.out.println("[SeatLockService] Seats locked successfully");
    }

    @Override
    public void unlockSeats(String showtimeId, List<String> seatIds, String userId) {
        System.out.println("[SeatLockService] Unlocking seats " + seatIds + " for user " + userId);
        lockRegistry.unlockAll(showtimeId, seatIds, userId);
        System.out.println("[SeatLockService] Seats unlocked successfully");
    }
}
