package com.movie.cinema_booking_backend.service.bookingticket.engine;

import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;

public interface PricingStep {

    void process(CalculationRequest request, CalculationResult result);
}
