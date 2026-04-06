package com.movie.cinema_booking_backend.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Body cho POST /api/admin/bookings
 *
 * Admin tạo booking ngoại lệ, bypass tất cả rule giới hạn ghế và giá.
 * Dành cho các trường hợp: bao rạp, sự kiện đặc biệt, khách VIP.
 */
@Getter
@Setter
public class AdminBookingRequest {

    @NotNull(message = "Suất chiếu không được để trống")
    private String showtimeId;

    @NotNull @NotEmpty(message = "Phải chọn ít nhất 1 ghế")
    private List<String> seatIds;

    @NotNull(message = "User ID không được để trống")
    private String userId;

    /**
     * Giá tổng do Admin chỉ định thủ công (override giá hệ thống).
     * Nếu null, hệ thống sẽ tính tự động qua PricingEngine.
     */
    private BigDecimal manualTotalAmount;

    /** Extra services (Optional) */
    private Map<Long, Integer> extras;

    /** Ghi chú nội dung hợp đồng / sự kiện */
    private String note;
}
