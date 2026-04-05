package com.movie.cinema_booking_backend.service.auth.observer;

public interface AccountStatusObserver {
    void update(AccountStatusChangedEvent event);
}
