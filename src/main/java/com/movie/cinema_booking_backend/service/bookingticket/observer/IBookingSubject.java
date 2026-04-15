package com.movie.cinema_booking_backend.service.bookingticket.observer;

/**
 * ════════════════════════════════════════════════════════════
 *  DESIGN PATTERN: OBSERVER — Subject Interface (GoF)
 * ════════════════════════════════════════════════════════════
 */
public interface IBookingSubject {
    void registerObserver(IBookingObserver observer);
    void removeObserver(IBookingObserver observer);
    void notifyObservers();
}
