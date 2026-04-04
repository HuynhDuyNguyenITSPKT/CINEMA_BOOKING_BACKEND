package com.movie.cinema_booking_backend.service.bookingticket.engine.step;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingStep;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import com.movie.cinema_booking_backend.service.bookingticket.engine.policy.PolicyFactory;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PromotionStep implements PricingStep {

    private final PolicyFactory policyFactory;

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        BigDecimal baseSubtotal = result.getBaseSubtotal();

        // Hỏi Policy tính discount (Group: thêm extra %; Standard: chỉ dùng mã)
        BigDecimal discount = policyFactory
                .getPolicy(request.bookingType())
                .calculateDiscount(baseSubtotal, request.promotion());

        result.setPromotionDiscount(discount);

        // Phân bổ discount về từng vé (chỉ trừ phần base, không chạm surcharge)
        distributeDiscount(request.seats(), result, discount);
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
