package com.movie.cinema_booking_backend.service.bookingticket.engine;

import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PricingEngine {

    private final List<PricingStep> steps;

    public CalculationResult calculate(CalculationRequest request) {
        CalculationResult result = new CalculationResult();
        for (PricingStep step : steps) {
            step.process(request, result);
        }
        return result;
    }
}
