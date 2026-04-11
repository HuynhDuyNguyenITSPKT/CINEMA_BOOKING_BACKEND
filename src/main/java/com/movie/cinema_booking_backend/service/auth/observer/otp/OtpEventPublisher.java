package com.movie.cinema_booking_backend.service.auth.observer.otp;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OtpEventPublisher {

    private final List<OtpObserver> observers;

    public OtpEventPublisher(List<OtpObserver> observers) {
        this.observers = observers;
    }

    public void notifyOtpGenerated(String email, String otp) {
        observers.forEach(observer -> observer.onOtpGenerated(email, otp));
    }
}