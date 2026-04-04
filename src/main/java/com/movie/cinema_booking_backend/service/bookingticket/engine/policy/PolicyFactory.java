package com.movie.cinema_booking_backend.service.bookingticket.engine.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: SIMPLE FACTORY
 * ═══════════════════════════════════════════════════════════
 *
 * Nơi DUY NHẤT trong hệ thống cần sửa khi thêm loại Booking mới.
 * Thêm CooperationBooking? → Tạo CooperationPricingPolicy + thêm 1 case ở đây.
 * Không cần chạm vào Engine, Builder, hay Service.
 */
@Component
@RequiredArgsConstructor
public class PolicyFactory {

    private final StandardPricingPolicy standard;
    private final GroupPricingPolicy    group;

    public PricingPolicy getPolicy(String bookingType) {
        if (bookingType == null) return standard;
        return switch (bookingType.toUpperCase()) {
            case "GROUP" -> group;
            default      -> standard;
        };
    }
}
