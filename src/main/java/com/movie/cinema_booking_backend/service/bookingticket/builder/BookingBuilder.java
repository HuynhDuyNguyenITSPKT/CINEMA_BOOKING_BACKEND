package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingEngine;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: BUILDER (GoF - Interface chuẩn)
 * ═══════════════════════════════════════════════════════════
 *
 * Phân định rõ 5 bước xây dựng một Booking:
 *  1. reset()         – Nhận input, xóa state cũ
 *  2. loadEntities()  – Lấy dữ liệu từ DB (User, Showtime, Seats, Extras)
 *  3. runPricing()    – Uỷ quyền TOÀN BỘ toán học cho PricingEngine
 *  4. buildEntities() – Đúc Entity Booking + Ticket từ CalculationResult
 *  5. getResult()     – Trả về sản phẩm cuối (chưa persist)
 *
 * Builder KHÔNG làm Toán – Toán là trách nhiệm duy nhất của PricingEngine.
 * Director kiểm soát thứ tự gọi các bước này.
 */
public interface BookingBuilder {

    void reset(BookingRequest request, String username);

    void loadEntities();

    void validateRules();

    void runPricing(PricingEngine engine);

    void buildEntities();

    Booking getResult();
}
