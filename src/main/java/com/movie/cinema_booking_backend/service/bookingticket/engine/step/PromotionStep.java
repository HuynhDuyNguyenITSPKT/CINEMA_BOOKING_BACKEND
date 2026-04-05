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

/**
 * ─── Trạm 3: Promotion & Discount ─────────────────────────
 * (1) Hỏi PolicyFactory lấy đúng Policy theo bookingType.
 * (2) Policy tính discount CHỈ trên baseSubtotal.
 * (3) Phân bổ discount xuống từng vé (fix rounding: vé cuối chịu phần dư).
 *
 * Nhờ PolicyFactory, trạm này không cần biết là Group hay Standard.
 * Thêm loại mới chỉ cần thêm Policy + đăng ký vào Factory.
 */
@Component
@Order(3)
public class PromotionStep implements PricingStep {

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        if (request.promotion() == null || !request.promotion().isActive()) {
            return; // No promo, do nothing
        }

        BigDecimal baseSubtotal = result.getBaseSubtotal();
        
        // Vì đã bỏ PolicyFactory (để đưa validate vào Builder), ta tính discount trực tiếp ở đây:
        BigDecimal discount = calculateDiscount(baseSubtotal, request.promotion());

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

    /**
     * Chia đều discount theo từng vé, vé cuối chịu phần dư của phép chia.
     * Đảm bảo tổng discount phân bổ = totalDiscount chính xác.
     */
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
                    ? totalDiscount.subtract(accumulated)   // Vé cuối gánh phần dư rounding
                    : perTicket;
            accumulated = accumulated.add(ticketDiscount);

            BigDecimal current = result.getTicketPrices().getOrDefault(seatId, BigDecimal.ZERO);
            result.putTicketPrice(seatId, current.subtract(ticketDiscount).max(BigDecimal.ZERO));
        }
    }
}
