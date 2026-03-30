package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.service.ISeatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SeatController {

    private final ISeatService seatService;

    public SeatController(ISeatService seatService) {
        this.seatService = seatService;
    }

    /**
     * GET /api/auditoriums/{id}/seats
     * Trả về sơ đồ ghế của phòng chiếu.
     *
     * Phase 1: status = null (chưa biết ghế có bị lock không).
     * Phase 2: SeatValidationProxy (@Primary) sẽ enrich status = AVAILABLE/LOCKED/BOOKED.
     */
    @GetMapping("/auditoriums/{id}/seats")
    public ApiResponse<?> getSeatsByAuditorium(@PathVariable String id) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy sơ đồ ghế thành công")
                .data(seatService.getSeatsByAuditorium(id))
                .build();
    }
}
