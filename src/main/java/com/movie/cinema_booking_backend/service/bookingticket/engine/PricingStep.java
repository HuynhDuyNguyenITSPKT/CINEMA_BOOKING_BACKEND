package com.movie.cinema_booking_backend.service.bookingticket.engine;

import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: CHAIN OF RESPONSIBILITY (GoF – Step Interface)
 * ═══════════════════════════════════════════════════════════
 *
 * Mỗi PricingStep xử lý một trạm duy nhất trong Pipeline:
 *   Order(1) BasePriceStep   – Tính tổng base price
 *   Order(2) SurchargeStep   – Tính phụ thu (không được giảm giá)
 *   Order(3) PromotionStep   – Áp dụng discount CHỈ trên base
 *   Order(4) TaxStep         – VAT 10% trên (base - discount + surcharge)
 *   Order(5) ExtrasStep      – Cộng đồ ăn/uống
 *
 * Lợi thế: Thêm trạm mới (HolidaySurcharge, LoyaltyDiscount...) chỉ cần
 *   tạo 1 class @Component + @Order(n) → không chạm code cũ (OCP).
 */
public interface PricingStep {

    void process(CalculationRequest request, CalculationResult result);
}
