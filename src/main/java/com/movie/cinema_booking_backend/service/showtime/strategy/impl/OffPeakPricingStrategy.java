package com.movie.cinema_booking_backend.service.showtime.strategy.impl;

import com.movie.cinema_booking_backend.service.showtime.strategy.IPricingStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * OffPeakPricingStrategy - Chiến lược giá giờ thấp điểm.
 *
 * Áp dụng khi:
 * - Ngày thường (Thứ 2 – Thứ 6) trước 17:00
 *
 * Mức giá: Giữ nguyên giá cơ sở (standardPrice × 1.0), không phụ thu.
 *
 * @Order(2): Được check sau PeakHourPricingStrategy.
 * Đây là fallback strategy — nếu không phải giờ cao điểm thì áp dụng giá thấp điểm.
 *
 * Strategy Pattern: Thay đổi logic giờ thấp điểm chỉ cần sửa class này.
 */
@Component
@Order(2)
public class OffPeakPricingStrategy implements IPricingStrategy {

    /** Mốc giờ bắt đầu tính là giờ cao điểm — thấp điểm là trước giờ này. */
    private static final int PEAK_HOUR_START = 17;

    /**
     * Kiểm tra xem thời điểm chiếu có thuộc giờ thấp điểm không.
     *
     * @param startTime thời gian bắt đầu buổi chiếu
     * @return true nếu là ngày thường (Thứ 2–6) và trước 17:00
     */
    @Override
    public boolean isApplicable(LocalDateTime startTime) {
        DayOfWeek day = startTime.getDayOfWeek();
        int hour = startTime.getHour();

        // Cuối tuần không phải giờ thấp điểm (đã được PeakHour xử lý)
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        // Ngày thường trước 17:00 → thấp điểm
        return hour < PEAK_HOUR_START;
    }

    /**
     * Tính giá vé giờ thấp điểm: giữ nguyên giá cơ sở.
     *
     * @param standardPrice giá cơ sở do admin thiết lập
     * @param startTime     thời gian chiếu (không dùng trong tính toán này)
     * @return giá vé = standardPrice (không thay đổi)
     */
    @Override
    public int calculatePrice(int standardPrice, LocalDateTime startTime) {
        return standardPrice;
    }
}
