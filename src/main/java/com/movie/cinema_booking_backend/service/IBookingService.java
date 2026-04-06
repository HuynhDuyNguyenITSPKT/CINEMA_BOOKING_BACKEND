package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.request.AdminBookingRequest;
import com.movie.cinema_booking_backend.response.BookingResponse;
import com.movie.cinema_booking_backend.response.PricePreviewResponse;

import java.util.List;

public interface IBookingService {
    /** Tạo booking draft qua đúng Template Method flow. */
    BookingResponse createBooking(BookingRequest request, String username);
    BookingResponse getBookingById(String bookingId, String username);
    List<BookingResponse> getMyBookings(String username);
    BookingResponse cancelBooking(String bookingId, String username);

    /**
     * Chạy Chain PricingEngine để tính giá preview (Preview-Only, KHÔNG lưu DB).
     * Frontend dùng để hiển thị hóa đơn realtime tại Checkout trước khi confirm.
     */
    PricePreviewResponse calculatePreviewPrice(BookingRequest request, String username);

    // ─── Admin Operations ─────────────────────────────────────────────────────
    /** Lấy toàn bộ booking, có lọc theo status (Admin only). */
    List<BookingResponse> getAllBookings(String status);

    /** Admin tạo booking ngoại lệ, bypass rules giới hạn ghế & giá tự động. */
    BookingResponse adminCreateBooking(AdminBookingRequest request);

    /** Duyệt đơn B2B (chỉ PENDING_APPROVAL -> RESERVED). */
    BookingResponse approveBooking(String bookingId);

    /** Admin hủy bất kỳ booking nào. */
    BookingResponse adminCancelBooking(String bookingId);
}

