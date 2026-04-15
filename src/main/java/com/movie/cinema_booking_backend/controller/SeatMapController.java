package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.SeatLockRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.service.ISeatLockService;
import com.movie.cinema_booking_backend.service.ISeatService;
import com.movie.cinema_booking_backend.service.bookingticket.singleton.SeatLockRegistry;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/showtimes")
@Validated
@RequiredArgsConstructor
public class SeatMapController {

    private final ISeatService seatService;
    private final ISeatLockService seatLockService;


    /**
     * GET /api/showtimes/{id}/seat-map
     *
     * Trả về sơ đồ ghế với trạng thái live:
     *   AVAILABLE — ghế trống.
     *   LOCKED    — đang bị user khác giữ tạm (in-RAM TTL 10 phút).
     *   BOOKED    — đã có Ticket xác nhận trong DB.
     *
     * authentication.getName() = username từ JWT → dùng làm userId.
     */
    @GetMapping("/{id}/seat-map")
    public ApiResponse<?> getSeatMap(@PathVariable("id") String id,
                                     Authentication authentication,
                                     HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        String userId = authentication != null ? authentication.getName() : "anonymous";
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy sơ đồ ghế thành công")
                .data(seatService.getSeatMapByShowtime(id, userId))
                .build();
    }

    /**
     * POST /api/showtimes/{id}/seats/lock
     *
     * Lock tạm một danh sách ghế cho user hiện tại (TTL 10 phút).
     * All-or-nothing: nếu bất kỳ ghế nào bị lock → rollback toàn bộ.
     *
     * Body: { "seatIds": ["seat-uuid-1", "seat-uuid-2"] }
     */
    @PostMapping("/{id}/seats/lock")
    public ApiResponse<?> lockSeats(@PathVariable("id") String id,
                                    @Valid @RequestBody SeatLockRequest request,
                                    Authentication authentication) {
        String userId = authentication.getName();
        seatLockService.lockSeats(id, request.getSeatIds(), userId,
                SeatLockRegistry.DEFAULT_TTL);
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lock ghế thành công. Giữ chỗ trong " +
                        SeatLockRegistry.DEFAULT_TTL.toMinutes() + " phút.")
                .build();
    }

    /**
     * DELETE /api/showtimes/{id}/seats/unlock
     *
     * Unlock ghế khi user thoát khỏi trang chọn ghế.
     * Chỉ unlock ghế của chính user đó (idempotent, không throw nếu ghế không lock).
     *
     * Body: { "seatIds": ["seat-uuid-1", "seat-uuid-2"] }
     */
    @DeleteMapping("/{id}/seats/unlock")
    public ApiResponse<?> unlockSeats(@PathVariable("id") String id,
                                      @Valid @RequestBody SeatLockRequest request,
                                      Authentication authentication) {
        String userId = authentication.getName();
        seatLockService.unlockSeats(id, request.getSeatIds(), userId);
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Unlock ghế thành công")
                .build();
    }

    /**
     * POST /api/showtimes/{id}/seats/unlock-on-exit
     *
     * Dành cho page reload/close: client dùng fetch keepalive để cố gắng gửi request
     * trước khi tab bị hủy.
     */
    @PostMapping("/{id}/seats/unlock-on-exit")
    public ApiResponse<?> unlockSeatsOnExit(@PathVariable("id") String id,
                                            @Valid @RequestBody SeatLockRequest request,
                                            Authentication authentication) {
        String userId = authentication.getName();
        seatLockService.unlockSeats(id, request.getSeatIds(), userId);
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Unlock ghế khi thoát trang thành công")
                .build();
    }
}
