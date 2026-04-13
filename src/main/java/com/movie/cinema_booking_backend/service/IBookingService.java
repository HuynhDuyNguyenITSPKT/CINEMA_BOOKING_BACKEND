package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.request.AdminUpdateGroupSeatsRequest;
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

    /**
     * Admin cập nhật danh sách ghế cho đơn B2B và duyệt đơn.
     * Gọi lại GroupBookingBuilder để tính lại giá + JPA diffing cập nhật Tickets.
     * Tranisiton: PENDING_APPROVAL → RESERVED.
     */
    BookingResponse updateGroupBookingSeatsAndApprove(String bookingId, AdminUpdateGroupSeatsRequest request);

    /** Duyệt đơn B2B đơn giản (chỉ PENDING_APPROVAL -> RESERVED, không đổi ghế). */
    BookingResponse approveBooking(String bookingId);

    /** Admin hủy bất kỳ booking nào. */
    BookingResponse adminCancelBooking(String bookingId);

    /** Dọn dẹp background (Cron Job): huỷ các ghế quá hạn thanh toán chưa hoàn thành */
    void cancelExpiredReservedBookings();
}

