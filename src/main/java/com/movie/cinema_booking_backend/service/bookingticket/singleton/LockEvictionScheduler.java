package com.movie.cinema_booking_backend.service.bookingticket.singleton;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LockEvictionScheduler {
    @Scheduled(fixedDelay = 30_000)
    public void cleanupExpiredLocks(){
        SeatLockRegistry.getInstance().evictExpired();
    }
}
