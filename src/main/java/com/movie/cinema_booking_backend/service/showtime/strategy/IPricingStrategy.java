package com.movie.cinema_booking_backend.service.showtime.strategy;

import java.time.LocalDateTime;

public interface IPricingStrategy {

    boolean isApplicable(LocalDateTime startTime);

    int calculatePrice(int standardPrice, LocalDateTime startTime);
}
