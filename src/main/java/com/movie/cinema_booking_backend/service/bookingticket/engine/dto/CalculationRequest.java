package com.movie.cinema_booking_backend.service.bookingticket.engine.dto;

import com.movie.cinema_booking_backend.entity.ExtraService;
import com.movie.cinema_booking_backend.entity.Promotion;

import java.math.BigDecimal;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: VALUE OBJECT (Immutable DTO)
 * ═══════════════════════════════════════════════════════════
 *
 * Đóng gói đầu vào cho PricingEngine. Builder tạo object này
 * từ các entity đã load từ DB, sau đó truyền vào Engine mà
 * KHÔNG chia sẻ state với bất kỳ Bean nào khác.
 *
 * Dùng Java Record để đảm bảo tính bất biến (Immutable).
 */
public record CalculationRequest(

        // Loại booking → PolicyFactory chọn đúng Policy
        String bookingType,

        // Giá gốc từ Showtime (int basePrice → chuyển sang BigDecimal)
        BigDecimal basePrice,

        // Danh sách ghế, mỗi ghế chứa surcharge riêng
        List<SeatInfo> seats,

        // Khuyến mãi áp dụng (nullable)
        Promotion promotion,

        // Đồ ăn / thức uống đi kèm
        List<ExtraLineItem> extras

) {

    /** Thông tin một ghế cần cho việc tính giá */
    public record SeatInfo(String seatId, BigDecimal surcharge) {}

    /** Một dòng extra service + số lượng */
    public record ExtraLineItem(ExtraService extraService, int quantity) {}
}
