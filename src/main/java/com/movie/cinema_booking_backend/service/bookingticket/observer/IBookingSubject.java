package com.movie.cinema_booking_backend.service.bookingticket.observer;

public interface IBookingSubject {
    void registerObserver(IBookingObserver observer);
    void removeObserver(IBookingObserver observer);
    void notifyObservers();
}
