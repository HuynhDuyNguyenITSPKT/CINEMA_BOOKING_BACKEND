package com.movie.cinema_booking_backend.service.showtime.strategy.impl;

import com.movie.cinema_booking_backend.service.showtime.strategy.IPricingStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * PeakHourPricingStrategy - Chiến lược giá giờ cao điểm.
 *
 * Áp dụng khi:
 * - Cuối tuần (Thứ 7, Chủ Nhật) — bất kỳ giờ nào
 * - Ngày thường (Thứ 2 – Thứ 6) từ 17:00 trở đi
 *
 * Mức giá: Cộng thêm 20% so với giá cơ sở (standardPrice × 1.2).
 *
 * @Order(1): Được check trước OffPeakPricingStrategy.
 * Strategy Pattern: Thay đổi mức phụ thu chỉ cần sửa class này, không ảnh hưởng gì khác.
 */
@Component
@Order(1)
public class PeakHourPricingStrategy implements IPricingStrategy {

    /** Hệ số nhân giá cho giờ cao điểm: giá tăng thêm 20%. */
    private static final double PEAK_PRICE_MULTIPLIER = 1.2;

    /** Mốc giờ bắt đầu tính là giờ cao điểm trong ngày thường (17:00). */
    private static final int PEAK_HOUR_START = 17;

    /**
     * Kiểm tra xem thời điểm chiếu có thuộc giờ cao điểm không.
     *
     * @param startTime thời gian bắt đầu buổi chiếu
     * @return true nếu là cuối tuần hoặc từ 17:00 trở đi trong ngày thường
     */
    @Override
    public boolean isApplicable(LocalDateTime startTime) {
        DayOfWeek day = startTime.getDayOfWeek();
        int hour = startTime.getHour();

        // Cuối tuần: áp dụng bất kể giờ nào
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return true;
        }
        // Ngày thường: áp dụng từ 17:00 trở đi (giờ cao điểm buổi tối)
        return hour >= PEAK_HOUR_START;
    }

    /**
     * Tính giá vé theo giờ cao điểm: cộng thêm 20%.
     *
     * @param standardPrice giá cơ sở do admin thiết lập
     * @param startTime     thời gian chiếu (không dùng trong tính toán này)
     * @return giá vé = standardPrice × 1.2 (làm tròn xuống)
     */
    @Override
    public int calculatePrice(int standardPrice, LocalDateTime startTime) {
        return (int) (standardPrice * PEAK_PRICE_MULTIPLIER);
    }
}
