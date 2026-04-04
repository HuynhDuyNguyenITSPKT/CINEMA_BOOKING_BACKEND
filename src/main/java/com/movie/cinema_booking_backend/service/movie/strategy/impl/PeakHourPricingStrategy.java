package com.movie.cinema_booking_backend.service.movie.strategy.impl;

import com.movie.cinema_booking_backend.service.movie.strategy.IPricingStrategy;
import com.movie.cinema_booking_backend.service.movie.strategy.PricingConstants;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Component
@Order(1)
public class PeakHourPricingStrategy implements IPricingStrategy {

    @Override
    public boolean isApplicable(LocalDateTime startTime) {
        DayOfWeek day = startTime.getDayOfWeek();
        int hour = startTime.getHour();

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return true;
        }
        return hour >= PricingConstants.PEAK_HOUR_START;
    }

    @Override
    public int calculatePrice(int standardPrice, LocalDateTime startTime) {
        return (int) (standardPrice * PricingConstants.PEAK_PRICE_MULTIPLIER);
    }
}
