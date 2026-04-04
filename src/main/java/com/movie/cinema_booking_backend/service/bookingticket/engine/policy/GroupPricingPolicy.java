package com.movie.cinema_booking_backend.service.bookingticket.engine.policy;

import com.movie.cinema_booking_backend.entity.Promotion;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Policy cho đặt vé nhóm (≥ minSeats ghế).
 *
 * validateEligibility: Kiểm tra đủ số ghế tối thiểu cho nhóm.
 * calculateDiscount:   Tính discount mã giảm giá (từ Standard) +
 *                      cộng thêm % chiết khấu đặc biệt theo nhóm.
 *
 * Dùng composition (inject StandardPricingPolicy) thay vì kế thừa
 * để tránh coupling giữa các Policy.
 */
@Component
@RequiredArgsConstructor
public class GroupPricingPolicy implements PricingPolicy {

    private final StandardPricingPolicy standard;

    @Value("${booking.group.min-seats:5}")
    private int minSeats;

    @Value("${booking.group.extra-discount-percent:5}")
    private int extraDiscountPercent;

    @Override
    public void validateEligibility(CalculationRequest request) {
        if (request.seats().size() < minSeats) {
            throw new AppException(ErrorCode.BOOKING_MIN_SEATS_REQUIRED);
        }
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal discountableAmount, Promotion promo) {
        // Lấy discount từ Standard (xử lý mã giảm giá)
        BigDecimal baseDiscount = standard.calculateDiscount(discountableAmount, promo);

        // Cộng thêm chiết khấu nhóm trên phần discountable
        if (extraDiscountPercent > 0) {
            BigDecimal extra = discountableAmount
                    .multiply(BigDecimal.valueOf(extraDiscountPercent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR);
            baseDiscount = baseDiscount.add(extra);
        }

        // Cap: discount không bao giờ vượt quá discountableAmount
        return baseDiscount.min(discountableAmount);
    }
}
