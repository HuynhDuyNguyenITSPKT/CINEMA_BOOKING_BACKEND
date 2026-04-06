package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.PricePreviewResponse;
import com.movie.cinema_booking_backend.service.IBookingService;
import com.movie.cinema_booking_backend.service.bookingticket.facade.BookingFacade;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@Validated
public class BookingController {

    private final IBookingService bookingService;
    private final BookingFacade bookingFacade;

    public BookingController(IBookingService bookingService, BookingFacade bookingFacade) {
        this.bookingService = bookingService;
        this.bookingFacade = bookingFacade;
    }

    /**
     * POST /api/bookings
     *
     * Tạo booking draft. Luồng:
     *   SeatValidationProxy.validateForBooking() (Phase 2)
     *   → BookingFlowTemplate.execute() (Phase 3 Template Method + Builder)
     *   → Booking PENDING trong DB
     *   → Phase 4 BookingFacade sẽ bổ sung thêm bước gọi IPayment
     *
     * bookingType = "STANDARD" (default) hoặc "GROUP" (≥5 ghế)
     */
    @PostMapping
    public ApiResponse<?> createBooking(@Valid @RequestBody BookingRequest request,
                                        Authentication authentication) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Khởi tạo đặt vé thành công, vui lòng thanh toán")
                .data(bookingFacade.initiateBooking(request, authentication.getName()))
                .build();
    }

    /**
     * GET /api/bookings/{id}
     * User chỉ xem booking của chính mình. Admin xem qua endpoint riêng (Phase 5).
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getBooking(@PathVariable("id") String id,
                                     Authentication authentication) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy thông tin đặt vé thành công")
                .data(bookingService.getBookingById(id, authentication.getName()))
                .build();
    }

    /**
     * GET /api/bookings/my
     * Lịch sử đặt vé của user hiện tại.
     */
    @GetMapping("/my")
    public ApiResponse<?> getMyBookings(Authentication authentication) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy lịch sử đặt vé thành công")
                .data(bookingService.getMyBookings(authentication.getName()))
                .build();
    }

    /**
     * POST /api/bookings/{id}/cancel
     * Huỷ booking PENDING. Booking SUCCESS cần refund flow (Phase 4).
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<?> cancelBooking(@PathVariable("id") String id,
                                        Authentication authentication) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Huỷ đặt vé thành công")
                .data(bookingService.cancelBooking(id, authentication.getName()))
                .build();
    }

    /**
     * POST /api/bookings/calculate-price
     *
     * Chạy Chain of Responsibility (PricingEngine) để tính giá preview.
     * KHÔNG lưu DB — Chỉ trả về hóa đơn bóc tách để Frontend hiển thị xem trước tại Checkout.
     * Đảm bảo giá hiển thị = giá sẽ thanh toán thật (zero drift).
     */
    @PostMapping("/calculate-price")
    public ApiResponse<PricePreviewResponse> calculatePrice(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        return new ApiResponse.Builder<PricePreviewResponse>()
                .success(true)
                .message("Tính giá thành công")
                .data(bookingService.calculatePreviewPrice(request, authentication.getName()))
                .build();
    }
}

