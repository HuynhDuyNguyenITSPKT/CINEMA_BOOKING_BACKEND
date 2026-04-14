package com.movie.cinema_booking_backend.service.bookingticket.engine.step;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingStep;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * ─── Trạm 1: Base Price ───────────────────────────────────
 * Tính tổng giá gốc: baseTicketPrice × số ghế.
 * Kết quả lưu vào result.baseSubtotal – đây là phần DUY NHẤT
 * mà Promotion được phép giảm giá.
 */
@Component
@Order(1)
public class BasePriceStep implements PricingStep {

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        BigDecimal subtotal = request.baseTicketPrice()
                .multiply(BigDecimal.valueOf(request.seats().size()));
        result.setBaseSubtotal(subtotal);
    }
}
