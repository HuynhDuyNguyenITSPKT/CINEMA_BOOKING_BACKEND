package com.movie.cinema_booking_backend.service.bookingticket.engine.step;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingStep;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * ─── Trạm 4: Group Discount ────────────────────────────────
 * Áp dụng chiết khấu 5% đặc quyền dành riêng cho Khách Đoàn (GROUP).
 *
 * Đặt TRƯỚC TaxStep (@Order 5) để đảm bảo Thuế VAT được tính toán
 * trên số tiền SAU KHI đã trừ chiết khấu đoàn → Fix triệt để Lỗ Hổng 2 & 3.
 *
 * Nếu bookingType != GROUP → bỏ qua, không tác động gì.
 * Open/Closed Principle: Thêm loại discount mới chỉ cần tạo Step mới.
 */
@Component
@Order(4)
public class GroupDiscountStep implements PricingStep {

    private static final BigDecimal GROUP_DISCOUNT_PERCENT = BigDecimal.valueOf(5);

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        if (!"GROUP".equalsIgnoreCase(request.bookingType())) {
            return; // Không phải khách đoàn → bỏ qua
        }

        BigDecimal baseSubtotal = result.getBaseSubtotal();
        if (baseSubtotal.compareTo(BigDecimal.ZERO) <= 0) return;

        // Tính 5% chiết khấu trên phần base (không kể surcharge, extras)
        BigDecimal groupDiscount = baseSubtotal
                .multiply(GROUP_DISCOUNT_PERCENT)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Cộng dồn vào promotionDiscount (không ghi đè, phòng trường hợp đã có PromotionStep)
        result.setPromotionDiscount(result.getPromotionDiscount().add(groupDiscount));

        // Fix Lỗ Hổng 3: Phân bổ discount đều xuống từng Ticket để ∑ticketPrices == finalTotal
        distributeDiscount(request.seats(), result, groupDiscount);
    }

    /**
     * Chia đều groupDiscount xuống từng vé theo tỷ lệ.
     * Vé cuối cùng chịu phần dư của phép chia (tránh lệch do rounding).
     */
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
                    ? totalDiscount.subtract(accumulated)  // Vé cuối gánh phần dư rounding
                    : perTicket;
            accumulated = accumulated.add(ticketDiscount);

            BigDecimal current = result.getTicketPrices().getOrDefault(seatId, BigDecimal.ZERO);
            result.putTicketPrice(seatId, current.subtract(ticketDiscount).max(BigDecimal.ZERO));
        }
    }
}
