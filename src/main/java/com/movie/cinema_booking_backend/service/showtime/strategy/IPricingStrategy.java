package com.movie.cinema_booking_backend.service.showtime.strategy;

import java.time.LocalDateTime;

/**
 * IPricingStrategy - Strategy Pattern Interface cho tính giá vé.
 *
 * Định nghĩa contract cho các chiến lược tính giá theo thời gian chiếu.
 *
 * Implementations hiện có:
 * - PeakHourPricingStrategy  (@Order 1): Cuối tuần hoặc giờ cao điểm (≥17:00) → +20%
 * - OffPeakPricingStrategy   (@Order 2): Ngày thường trước 17:00 → giá gốc
 *
 * SOLID — Open/Closed: Thêm strategy mới (VD: HolidayPricingStrategy) không cần sửa context.
 * SOLID — Liskov Substitution: Các implementation có thể thay thế nhau hoàn toàn.
 * SOLID — Dependency Inversion: PricingStrategyContext phụ thuộc vào interface này.
 *
 * Cách thêm chiến lược mới:
 * 1. Tạo class implements IPricingStrategy
 * 2. Đánh @Component + @Order(n) với n phù hợp
 * 3. Implement isApplicable() và calculatePrice()
 * → Context sẽ tự inject và sử dụng — không cần sửa bất kỳ class nào khác.
 */
public interface IPricingStrategy {

    /**
     * Kiểm tra liệu chiến lược này có áp dụng cho thời điểm chiếu hay không.
     * Context sẽ duyệt theo @Order và dùng strategy đầu tiên trả về true.
     *
     * @param startTime thời gian bắt đầu buổi chiếu
     * @return true nếu chiến lược này phù hợp với thời điểm đó
     */
    boolean isApplicable(LocalDateTime startTime);

    /**
     * Tính giá vé cuối cùng dựa trên chiến lược này.
     * Chỉ được gọi sau khi isApplicable() trả về true.
     *
     * @param standardPrice giá cơ sở do admin thiết lập
     * @param startTime     thời gian chiếu (có thể dùng để tính giá dynamic)
     * @return giá vé sau khi áp dụng chiến lược
     */
    int calculatePrice(int standardPrice, LocalDateTime startTime);
}
