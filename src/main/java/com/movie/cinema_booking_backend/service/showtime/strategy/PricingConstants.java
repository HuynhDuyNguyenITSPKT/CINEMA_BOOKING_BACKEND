package com.movie.cinema_booking_backend.service.showtime.strategy;

/**
 * PricingConstants — Hằng số dùng chung cho các Pricing Strategy.
 *
 * <p>Tập trung tất cả magic number liên quan đến tính giá vé tại một chỗ.
 * Khi cần thay đổi mốc giờ hay hệ số giá, chỉ cần sửa tại đây.
 *
 * <p>DRY: Tránh duplicate constant ở nhiều strategy class.
 * OOP: Utility class — constructor private, không cho khởi tạo.
 */
public final class PricingConstants {

    /** Mốc giờ bắt đầu tính là giờ cao điểm trong ngày thường (17:00). */
    public static final int PEAK_HOUR_START = 17;

    /** Hệ số nhân giá cho giờ cao điểm: tăng thêm 20%. */
    public static final double PEAK_PRICE_MULTIPLIER = 1.2;

    /** Utility class — không cho phép khởi tạo. */
    private PricingConstants() {}
}
