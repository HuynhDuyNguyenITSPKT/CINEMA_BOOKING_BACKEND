package com.movie.cinema_booking_backend.service.bookingticket.observer.impl;

import com.movie.cinema_booking_backend.service.ISeatLockService;
import com.movie.cinema_booking_backend.service.bookingticket.observer.BookingPaymentSubject;
import com.movie.cinema_booking_backend.service.bookingticket.observer.BookingSuccessEvent;
import com.movie.cinema_booking_backend.service.bookingticket.observer.IBookingObserver;
import com.movie.cinema_booking_backend.service.bookingticket.observer.IBookingSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatUnlockObserver implements IBookingObserver {

    private final ISeatLockService seatLockService;

    @Override
    public void update(IBookingSubject subject) {
        if (subject instanceof BookingPaymentSubject concreteSubject) {
            BookingSuccessEvent event = concreteSubject.getState();
            
            if (event.showtimeId() == null || event.seatIds() == null || event.seatIds().isEmpty()) {
                return;
            }

            log.info("[SeatUnlockObserver] Thanh toán Unit {} xong. Giải phóng {} ghế đang bị Lock trong RAM", 
                     event.bookingId(), event.seatIds().size());

            try {
                seatLockService.forceUnlockSeats(event.showtimeId(), event.seatIds());
            } catch (Exception e) {
                 log.error("[SeatUnlockObserver] Lỗi khi unlock ghế: {}", e.getMessage());
            }
        }
    }
}
