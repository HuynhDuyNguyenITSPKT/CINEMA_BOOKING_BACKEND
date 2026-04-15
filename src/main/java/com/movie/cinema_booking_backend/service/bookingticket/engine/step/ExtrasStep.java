package com.movie.cinema_booking_backend.service.bookingticket.engine.step;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingStep;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(6)
public class ExtrasStep implements PricingStep {

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        BigDecimal total = BigDecimal.ZERO;

        for (CalculationRequest.ExtraLineItem item : request.extras()) {
            total = total.add(
                    item.extraService().getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.quantity()))
            );
        }
        result.setExtrasTotal(total);
    }
}
