package com.movie.cinema_booking_backend.service.bookingticket.engine.step;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingStep;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Order(2)
public class SurchargeStep implements PricingStep {

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        BigDecimal base       = request.baseTicketPrice();
        BigDecimal surchTotal = BigDecimal.ZERO;

        for (CalculationRequest.SeatInfo seat : request.seats()) {
            BigDecimal surcharge = seat.surchargeAmount();
            surchTotal = surchTotal.add(surcharge);

            BigDecimal rawPrice = base.add(surcharge).setScale(2, RoundingMode.HALF_UP);
            result.putTicketPrice(seat.seatId(), rawPrice);
        }

        result.setSurchargesTotal(surchTotal);
    }
}
