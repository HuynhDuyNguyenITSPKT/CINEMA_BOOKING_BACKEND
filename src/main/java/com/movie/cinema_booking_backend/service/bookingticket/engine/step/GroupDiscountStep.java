package com.movie.cinema_booking_backend.service.bookingticket.engine.step;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingStep;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@Order(4)
public class GroupDiscountStep implements PricingStep {

    private static final BigDecimal GROUP_DISCOUNT_PERCENT = BigDecimal.valueOf(5);

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        if (!"GROUP".equalsIgnoreCase(request.bookingType())) {
            return;
        }

        BigDecimal baseSubtotal = result.getBaseSubtotal();
        if (baseSubtotal.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal groupDiscount = baseSubtotal
                .multiply(GROUP_DISCOUNT_PERCENT)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        result.setPromotionDiscount(result.getPromotionDiscount().add(groupDiscount));

        distributeDiscount(request.seats(), result, groupDiscount);
    }

    private void distributeDiscount(List<CalculationRequest.SeatInfo> seats,
                                    CalculationResult result,
                                    BigDecimal totalDiscount) {
        if (seats.isEmpty() || totalDiscount.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal perTicket = totalDiscount.divide(
                BigDecimal.valueOf(seats.size()), 2, RoundingMode.FLOOR);
        BigDecimal accumulated = BigDecimal.ZERO;

        for (int i = 0; i < seats.size(); i++) {
            String seatId = seats.get(i).seatId();

            BigDecimal ticketDiscount = (i == seats.size() - 1)
                    ? totalDiscount.subtract(accumulated)
                    : perTicket;
            accumulated = accumulated.add(ticketDiscount);

            BigDecimal current = result.getTicketPrices().getOrDefault(seatId, BigDecimal.ZERO);
            result.putTicketPrice(seatId, current.subtract(ticketDiscount).max(BigDecimal.ZERO));
        }
    }
}
