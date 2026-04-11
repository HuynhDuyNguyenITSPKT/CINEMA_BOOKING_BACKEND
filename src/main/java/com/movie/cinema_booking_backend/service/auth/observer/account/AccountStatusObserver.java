package com.movie.cinema_booking_backend.service.auth.observer.account;

public interface AccountStatusObserver {
    void update(AccountStatusChangedEvent event);
}