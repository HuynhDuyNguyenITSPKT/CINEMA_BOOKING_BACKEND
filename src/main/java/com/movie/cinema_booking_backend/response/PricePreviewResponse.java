package com.movie.cinema_booking_backend.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Response cho POST /api/bookings/calculate-price
 *
 * Chứa bóc tách chi tiết hóa đơn để Frontend render "Biên lai xem trước".
 * Mỗi field map trực tiếp với một field của CalculationResult.
 *
 * Frontend UI sẽ cộng gộp để hiển thị đơn giản:
 *   Tiền vé hiển thị  = baseSubtotal + surchargesTotal
 *   Giảm giá hiển thị = promotionDiscount
 *   Bắp nước          = extrasTotal
 *   VAT               = taxAmount
 *   ─────────────────────────────
 *   TỔNG THANH TOÁN   = finalTotal
 */
@Getter
@Builder
public class PricePreviewResponse {
    private BigDecimal baseSubtotal;      // Tiền vé gốc theo ghế
    private BigDecimal surchargesTotal;   // Phụ thu VIP/Sweetbox
    private BigDecimal promotionDiscount; // Tổng giảm giá (promo + group 5%)
    private BigDecimal extrasTotal;       // Bắp nước
    private BigDecimal taxAmount;         // VAT 10%
    private BigDecimal finalTotal;        // TỔNG THANH TOÁN

    /** Mô tả promotion đã áp dụng (hiển thị trên UI) */
    private String promotionDescription;

    /** Cờ báo đây là luồng Group Booking (UI disable ô promo code) */
    private boolean isGroupBooking;
}
