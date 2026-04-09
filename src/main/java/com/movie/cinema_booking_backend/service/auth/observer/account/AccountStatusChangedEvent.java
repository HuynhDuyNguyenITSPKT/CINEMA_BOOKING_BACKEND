package com.movie.cinema_booking_backend.service.auth.observer.account;

public record AccountStatusChangedEvent(
        String username,
        String email,
        String fullName,
        boolean active
) {
}