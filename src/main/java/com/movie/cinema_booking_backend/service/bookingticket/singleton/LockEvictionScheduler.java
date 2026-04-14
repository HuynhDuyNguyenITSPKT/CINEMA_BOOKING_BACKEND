package com.movie.cinema_booking_backend.service.bookingticket.singleton;

import com.movie.cinema_booking_backend.service.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockEvictionScheduler {

    private final IBookingService bookingService;

    @Scheduled(fixedDelay = 60_000)
    public void cleanupExpiredLocks() {
        // 1. Dọn dẹp RAM Seat Lock
        SeatLockRegistry.getInstance().evictExpired();
        
        // 2. Dọn dẹp Database (Bỏ ngang thanh toán)
        bookingService.cancelExpiredReservedBookings();
    }
}
