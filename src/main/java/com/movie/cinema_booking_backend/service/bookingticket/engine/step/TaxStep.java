package com.movie.cinema_booking_backend.service.bookingticket.engine.step;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingStep;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ─── Trạm 4: VAT ──────────────────────────────────────────
 * Tính thuế VAT trên phần taxable:
 *   taxable = baseSubtotal - promotionDiscount + surchargesTotal (≥ 0)
 *
 * Extras (đồ ăn/uống) KHÔNG chịu thuế – chúng đã có giá lẻ bao gồm VAT.
 *
 * Cấu hình qua application.properties:
 *   booking.tax.vat-percent=10    (mặc định 10%)
 *   booking.tax.vat-percent=0     (để tắt nếu giá đã bao gồm VAT)
 */
@Component
@Order(4)
public class TaxStep implements PricingStep {

    @Value("${booking.tax.vat-percent:10}")
    private int vatPercent;

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        if (vatPercent <= 0) return;

        BigDecimal taxable = result.getBaseSubtotal()
                .subtract(result.getPromotionDiscount())
                .add(result.getSurchargesTotal())
                .max(BigDecimal.ZERO);

        BigDecimal tax = taxable
                .multiply(BigDecimal.valueOf(vatPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        result.setTaxAmount(tax);
    }
}
