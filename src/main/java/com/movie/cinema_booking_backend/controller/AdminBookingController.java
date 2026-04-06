package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.AdminBookingRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.service.IBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AdminBookingController — Endpoint CRUD Booking dành riêng cho Admin.
 *
 * Endpoints:
 *  GET    /api/admin/bookings              — Xem tất cả booking (lọc theo status)
 *  POST   /api/admin/bookings              — Tạo booking ngoại lệ (bypass rules & giá)
 *  PUT    /api/admin/bookings/{id}/approve — Duyệt đơn B2B (chỉ PENDING_APPROVAL)
 *  PUT    /api/admin/bookings/{id}/cancel  — Hủy bất kỳ booking nào
 */
@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final IBookingService bookingService;

    @GetMapping
    public ApiResponse<?> getAllBookings(@RequestParam(required = false) String status) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy danh sách đặt vé thành công")
                .data(bookingService.getAllBookings(status))
                .build();
    }

    /**
     * POST /api/admin/bookings
     *
     * Admin tạo booking ngoại lệ:
     * - Bypass max seat limit (8 ghế của Standard)
     * - Nếu manualTotalAmount != null: Override tổng tiền theo giá thỏa thuận
     * - Dùng cho: bao rạp sự kiện, khách VIP, khách đòi giá riêng
     */
    @PostMapping
    public ApiResponse<?> adminCreateBooking(@Valid @RequestBody AdminBookingRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Tạo đặt vé thành công")
                .data(bookingService.adminCreateBooking(request))
                .build();
    }

    /**
     * PUT /api/admin/bookings/{id}/approve
     *
     * Duyệt đơn Khách Đoàn B2B.
     * Backend chặn cứng: Chỉ đơn có status = PENDING_APPROVAL mới được duyệt.
     * Tránh Admin nhầm lẫn approve vé Standard/Couple.
     */
    @PutMapping("/{id}/approve")
    public ApiResponse<?> approveBooking(@PathVariable("id") String id) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Duyệt đặt vé thành công")
                .data(bookingService.approveBooking(id))
                .build();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<?> adminCancelBooking(@PathVariable("id") String id) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Huỷ đặt vé thành công")
                .data(bookingService.adminCancelBooking(id))
                .build();
    }
}
