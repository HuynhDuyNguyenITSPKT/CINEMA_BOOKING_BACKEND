package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.AdminUpdateGroupSeatsRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.service.IBookingService;
import com.movie.cinema_booking_backend.service.bookingticket.facade.BookingFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AdminBookingController — Endpoints Booking dành cho Admin.
 *
 * Luồng B2B hoàn chỉnh:
 *  GET    /api/admin/bookings                     — Danh sách (lọc theo status)
 *  PUT    /api/admin/bookings/{id}/update-seats   — Admin chỉnh sửa ghế + duyệt đơn B2B
 *  PUT    /api/admin/bookings/{id}/approve        — Duyệt nhanh (giữ nguyên ghế)
 *  PUT    /api/admin/bookings/{id}/cancel         — Hủy bất kỳ đơn nào
 */
@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final IBookingService bookingService;
    private final BookingFacade bookingFacade;

    @GetMapping
    public ApiResponse<?> getAllBookings(@RequestParam(required = false) String status) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy danh sách đặt vé thành công")
                .data(bookingService.getAllBookings(status))
                .build();
    }

    /**
     * PUT /api/admin/bookings/{id}/update-seats
     *
     * Admin xác nhận danh sách ghế cuối cùng sau khi thương lượng với khách.
     * Hệ thống gọi GroupBookingBuilder để tạo "bản vẽ" mới với Pipeline tính giá:
     *  - GroupDiscountStep: Giảm 5% B2B
     *  - TaxStep: Cộng 10% VAT
     * Sau đó JPA Diffing đồng bộ Tickets (xóa ghế bỏ, thêm ghế mới).
     * Trạng thái chuyển từ PENDING_APPROVAL → RESERVED.
     */
    @PutMapping("/{id}/update-seats")
    public ApiResponse<?> updateGroupSeatsAndApprove(
            @PathVariable("id") String id,
            @RequestBody AdminUpdateGroupSeatsRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Cập nhật ghế và duyệt đơn B2B thành công")
                .data(bookingService.updateGroupBookingSeatsAndApprove(id, request))
                .build();
    }

    /**
     * PUT /api/admin/bookings/{id}/approve
     *
     * Duyệt nhanh đơn B2B khi không cần đổi ghế.
     * Chỉ chuyển trạng thái PENDING_APPROVAL → RESERVED.
     */
    @PutMapping("/{id}/approve")
    public ApiResponse<?> approveBooking(@PathVariable("id") String id) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Duyệt đặt vé thành công")
                .data(bookingFacade.approveBooking(id))
                .build();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<?> adminCancelBooking(@PathVariable("id") String id) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Huỷ đặt vé thành công")
                .data(bookingFacade.adminCancelBooking(id))
                .build();
    }
}
