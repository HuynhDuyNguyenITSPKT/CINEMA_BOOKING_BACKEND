package com.movie.cinema_booking_backend.service.auth.observer;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AccountStatusSubject {

    private final List<AccountStatusObserver> observers = new CopyOnWriteArrayList<>();

    public AccountStatusSubject(List<AccountStatusObserver> initialObservers) {
        if (initialObservers != null && !initialObservers.isEmpty()) {
            observers.addAll(initialObservers);
        }
    }

    public void attach(AccountStatusObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public void detach(AccountStatusObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(AccountStatusChangedEvent event) {
        for (AccountStatusObserver observer : observers) {
            observer.update(event);
        }
    }
}
