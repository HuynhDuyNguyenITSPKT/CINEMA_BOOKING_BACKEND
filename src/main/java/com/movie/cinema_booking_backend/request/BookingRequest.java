package com.movie.cinema_booking_backend.request;

import com.movie.cinema_booking_backend.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Body cho POST /api/bookings/draft
 *
 * Ví dụ:
 * {
 *   "showtimeId": "uuid",
 *   "seatIds": ["uuid1", "uuid2"],
 *   "extras": {"1": 2, "3": 1},   // {extraServiceId: quantity}
 *   "promotionCode": "SUMMER2025",
 *   "paymentMethod": "MOMO"
 * }
 */
@Getter
@Setter
public class BookingRequest {

    @NotNull(message = "Suất chiếu không được để trống")
    private String showtimeId;

    @NotNull @NotEmpty(message = "Phải chọn ít nhất 1 ghế")
    private List<String> seatIds;

    /**
     * Map từ extraServiceId → số lượng.
     * Nullable — không bắt buộc chọn đồ ăn/uống.
     */
    private Map<Long, Integer> extras = new java.util.HashMap<>();

    /** Mã khuyến mãi. Nullable — không bắt buộc. */
    private String promotionCode;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    /** Ghi chú đặc biệt (dành cho GroupBookingFlow). Nullable. */
    private String note;

    /**
     * Loại luồng booking. "STANDARD" (default) hoặc "GROUP".
     * Ảnh hưởng đến Template Method được chọn.
     */
    private String bookingType = "STANDARD";
}
