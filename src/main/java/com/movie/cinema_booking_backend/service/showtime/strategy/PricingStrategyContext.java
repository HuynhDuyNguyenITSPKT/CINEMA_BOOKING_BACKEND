package com.movie.cinema_booking_backend.service.showtime.strategy;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * PricingStrategyContext - Strategy Pattern Context.
 *
 * Nhận danh sách IPricingStrategy từ Spring (theo @Order) và chọn chiến lược phù hợp
 * dựa trên thời gian chiếu phim.
 *
 * Luồng làm việc:
 *   1. Spring inject tất cả IPricingStrategy beans theo thứ tự @Order
 *   2. Context duyệt qua danh sách, tìm strategy đầu tiên có isApplicable() == true
 *   3. Gọi calculatePrice() của strategy đó và trả về kết quả
 *
 * SOLID — Single Responsibility: Chỉ có nhiệm vụ chọn đúng strategy, không tính giá.
 * SOLID — Open/Closed: Thêm strategy mới bằng cách add @Component class, không sửa class này.
 * SOLID — Dependency Inversion: Phụ thuộc vào List<IPricingStrategy> (abstraction), không concrete.
 */
@Component
public class PricingStrategyContext {

    private final List<IPricingStrategy> strategies;

    /**
     * Spring tự động inject tất cả IPricingStrategy beans theo thứ tự @Order.
     * PeakHourPricingStrategy (@Order 1) được check trước OffPeakPricingStrategy (@Order 2).
     *
     * @param strategies danh sách strategy được Spring inject theo @Order
     */
    public PricingStrategyContext(List<IPricingStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Chọn chiến lược giá phù hợp và tính giá vé cuối cùng.
     *
     * Mọi thời điểm trong ngày đều được cover bởi ít nhất một strategy:
     * - Cuối tuần bất kỳ giờ nào → PeakHour (check trước, @Order 1)
     * - Thứ 2-6 từ 17:00+ → PeakHour
     * - Thứ 2-6 trước 17:00 → OffPeak (@Order 2)
     *
     * @param standardPrice giá cơ sở do admin thiết lập
     * @param startTime     thời gian bắt đầu buổi chiếu
     * @return giá vé cuối cùng sau khi áp dụng strategy phù hợp
     * @throws IllegalStateException nếu không có strategy nào phù hợp (không xảy ra với setup hiện tại)
     */
    public int getPrice(int standardPrice, LocalDateTime startTime) {
        IPricingStrategy applicableStrategy = strategies.stream()
                .filter(strategy -> strategy.isApplicable(startTime))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy chiến lược giá phù hợp cho thời gian: " + startTime));

        return applicableStrategy.calculatePrice(standardPrice, startTime);
    }
}
