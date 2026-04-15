package com.movie.cinema_booking_backend.service.bookingticket.engine.step;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingStep;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import com.movie.cinema_booking_backend.entity.Promotion;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@Order(3)
public class PromotionStep implements PricingStep {

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        if (request.promotion() == null || !request.promotion().isActive()) {
            return;
        }

        BigDecimal discountableAmount = result.getBaseSubtotal()
                .add(result.getSurchargesTotal());

        BigDecimal discount = calculateDiscount(discountableAmount, request.promotion());

        result.setPromotionDiscount(discount);
        distributeDiscount(request.seats(), result, discount);
    }

    private BigDecimal calculateDiscount(BigDecimal discountableAmount, Promotion promo) {
        if (promo.getMinOrderValue() != null
                && promo.getMinOrderValue().compareTo(BigDecimal.ZERO) > 0
                && discountableAmount.compareTo(promo.getMinOrderValue()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = switch (promo.getDiscountType()) {
            case PERCENTAGE -> discountableAmount
                    .multiply(promo.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR);
            case FIXED_AMOUNT -> promo.getDiscountValue().min(discountableAmount);
        };

        if (promo.getMaxDiscountAmount() != null
                && promo.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(promo.getMaxDiscountAmount());
        }
        return discount;
    }

    private void distributeDiscount(List<CalculationRequest.SeatInfo> seats,
                                    CalculationResult result,
                                    BigDecimal totalDiscount) {
        if (seats.isEmpty() || totalDiscount.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal perTicket   = totalDiscount.divide(
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
