package com.movie.cinema_booking_backend.service.bookingticket.engine.policy;

import com.movie.cinema_booking_backend.entity.Promotion;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;

import java.math.BigDecimal;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: POLICY (Strategy + Rule)
 * ═══════════════════════════════════════════════════════════
 *
 * Tách biệt HAI trách nhiệm:
 *  1. validateEligibility() – Kiểm tra điều kiện nghiệp vụ (min/max ghế).
 *                             Gọi trước khi tính tiền để fail-fast.
 *  2. calculateDiscount()   – Quyết định mức discount theo loại booking.
 *                             STANDARD: áp dụng mã giảm giá đơn thuần.
 *                             GROUP: cộng thêm % chiết khấu nhóm.
 *
 * Khi thêm loại Booking mới (CooperationBooking, CoupleBooking...):
 *   → Chỉ tạo 1 class mới implement interface này + đăng ký vào PolicyFactory.
 *   → Không cần chạm vào Engine hay Builder.
 */
public interface PricingPolicy {

    /** Kiểm tra rule nghiệp vụ. Ném AppException nếu không hợp lệ. */
    void validateEligibility(CalculationRequest request);

    /**
     * Tính tổng discount áp dụng cho booking.
     *
     * @param discountableAmount Phần được phép giảm giá (baseSubtotal, KHÔNG bao gồm surcharge).
     * @param promotion          Mã giảm giá do user nhập (nullable).
     * @return Số tiền giảm (≥ 0). Không bao giờ vượt quá discountableAmount.
     */
    BigDecimal calculateDiscount(BigDecimal discountableAmount, Promotion promotion);
}
