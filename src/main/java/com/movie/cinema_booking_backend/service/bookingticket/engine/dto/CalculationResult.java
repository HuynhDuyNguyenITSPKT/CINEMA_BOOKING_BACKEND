package com.movie.cinema_booking_backend.service.bookingticket.engine.dto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: VALUE OBJECT (Mutable accumulator)
 * ═══════════════════════════════════════════════════════════
 *
 * Được các PricingStep điền vào tuần tự qua Pipeline.
 * Sau khi Engine chạy xong, Builder đọc kết quả này để tạo
 * Booking entity và danh sách Ticket với giá chính xác.
 *
 * Cấu trúc tách biệt:
 *   baseSubtotal      – chỉ base price (phần được phép giảm giá)
 *   surchargesTotal   – phụ thu ghế VIP/Sweetbox (KHÔNG giảm giá)
 *   promotionDiscount – discount tính trên baseSubtotal
 *   taxAmount         – VAT 10% tính trên (base - discount + surcharge)
 *   extrasTotal       – đồ ăn/uống (không thuế, không giảm giá)
 */
public class CalculationResult {

    private BigDecimal baseSubtotal      = BigDecimal.ZERO;
    private BigDecimal surchargesTotal   = BigDecimal.ZERO;
    private BigDecimal promotionDiscount = BigDecimal.ZERO;
    private BigDecimal taxAmount         = BigDecimal.ZERO;
    private BigDecimal extrasTotal       = BigDecimal.ZERO;

    /**
     * Map: seatId → giá vé cuối cùng (đã bao gồm surcharge, đã trừ discount một phần).
     * Builder sẽ đọc map này để tạo từng Ticket.
     */
    private final Map<String, BigDecimal> ticketPrices = new LinkedHashMap<>();

    /**
     * Tổng tiền thanh toán cuối cùng.
     * Công thức: (base - discount + surcharge + tax) + extras ≥ 0
     */
    public BigDecimal getFinalTotal() {
        return baseSubtotal
                .subtract(promotionDiscount)
                .add(surchargesTotal)
                .add(taxAmount)
                .add(extrasTotal)
                .max(BigDecimal.ZERO);
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public BigDecimal getBaseSubtotal()      { return baseSubtotal; }
    public void setBaseSubtotal(BigDecimal v) { this.baseSubtotal = v; }

    public BigDecimal getSurchargesTotal()      { return surchargesTotal; }
    public void setSurchargesTotal(BigDecimal v) { this.surchargesTotal = v; }

    public BigDecimal getPromotionDiscount()      { return promotionDiscount; }
    public void setPromotionDiscount(BigDecimal v) { this.promotionDiscount = v; }

    public BigDecimal getTaxAmount()      { return taxAmount; }
    public void setTaxAmount(BigDecimal v) { this.taxAmount = v; }

    public BigDecimal getExtrasTotal()      { return extrasTotal; }
    public void setExtrasTotal(BigDecimal v) { this.extrasTotal = v; }

    public Map<String, BigDecimal> getTicketPrices() { return ticketPrices; }
    public void putTicketPrice(String seatId, BigDecimal price) {
        ticketPrices.put(seatId, price);
    }
}
