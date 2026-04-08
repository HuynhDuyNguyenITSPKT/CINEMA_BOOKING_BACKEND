package com.movie.cinema_booking_backend.service.auth.observer.account;

import com.movie.cinema_booking_backend.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountStatusEmailObserver implements AccountStatusObserver {

    private final IEmailService emailService;

    @Override
    public void update(AccountStatusChangedEvent event) {
        emailService.sendAccountStatusChangedEmail(event.email(), event.fullName(), event.active());
    }
}