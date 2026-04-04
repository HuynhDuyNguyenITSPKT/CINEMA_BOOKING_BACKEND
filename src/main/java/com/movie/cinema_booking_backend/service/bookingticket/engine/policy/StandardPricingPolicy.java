package com.movie.cinema_booking_backend.service.bookingticket.engine.policy;

import com.movie.cinema_booking_backend.entity.Promotion;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Policy cho đặt vé thông thường (1 đến max ghế).
 *
 * validateEligibility: Kiểm tra không vượt quá số ghế tối đa.
 * calculateDiscount:   Áp dụng mã giảm giá theo DiscountType (PERCENTAGE / FIXED_AMOUNT).
 *                      Tôn trọng minOrderValue và maxDiscountAmount của Promotion.
 */
@Component
public class StandardPricingPolicy implements PricingPolicy {

    @Value("${booking.standard.max-seats:8}")
    private int maxSeats;

    @Override
    public void validateEligibility(CalculationRequest request) {
        if (request.seats().size() > maxSeats) {
            throw new AppException(ErrorCode.BOOKING_MAX_SEATS_EXCEEDED);
        }
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal discountableAmount, Promotion promo) {
        if (promo == null || !promo.isActive()) return BigDecimal.ZERO;

        // Kiểm tra giá trị đơn tối thiểu
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

        // Áp dụng cap maxDiscountAmount nếu có
        if (promo.getMaxDiscountAmount() != null
                && promo.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(promo.getMaxDiscountAmount());
        }

        return discount;
    }
}
