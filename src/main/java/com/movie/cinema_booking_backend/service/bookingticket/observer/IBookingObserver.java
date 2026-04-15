package com.movie.cinema_booking_backend.service.bookingticket.observer;

/**
 * ════════════════════════════════════════════════════════════
 *  DESIGN PATTERN: OBSERVER — Observer Interface (GoF)
 * ════════════════════════════════════════════════════════════
 */
public interface IBookingObserver {
    void update(IBookingSubject subject);
}
