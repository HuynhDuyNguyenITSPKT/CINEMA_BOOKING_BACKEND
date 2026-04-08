package com.movie.cinema_booking_backend.service.auth.observer.account;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountStatusSubject {

    private final List<AccountStatusObserver> observers;

    public AccountStatusSubject(List<AccountStatusObserver> observers) {
        this.observers = observers;
    }

    public void notifyAccountStatusChanged(AccountStatusChangedEvent event) {
        observers.forEach(observer -> observer.update(event));
    }
}