package com.movie.cinema_booking_backend.service.showtime.strategy.impl;

import com.movie.cinema_booking_backend.service.showtime.strategy.IPricingStrategy;
import com.movie.cinema_booking_backend.service.showtime.strategy.PricingConstants;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Component
@Order(2)
public class OffPeakPricingStrategy implements IPricingStrategy {

    @Override
    public boolean isApplicable(LocalDateTime startTime) {
        DayOfWeek day = startTime.getDayOfWeek();
        int hour = startTime.getHour();

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        return hour < PricingConstants.PEAK_HOUR_START;
    }

    @Override
    public int calculatePrice(int standardPrice, LocalDateTime startTime) {
        return standardPrice;
    }
}
