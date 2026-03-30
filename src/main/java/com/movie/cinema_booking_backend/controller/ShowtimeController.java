package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.ShowtimeRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.IShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ShowtimeController — Quản lý API tạo lịch chiếu (Admin only).
 *
 * <p>Tất cả endpoints được bảo vệ bởi role ADMIN thông qua SecurityConfig.
 * Controller chỉ nhận request, delegate sang service, trả về response — không chứa business logic.
 *
 * <p>Base path: {@code /api/admin/showtimes}
 */
@RestController
@RequestMapping("/api/admin/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final IShowtimeService showtimeService;

    /**
     * Tạo lịch chiếu mới.
     *
     * <p>Strategy Pattern (trong service): Tự động áp dụng giá cao điểm/thấp điểm
     * dựa trên {@code startTime} của buổi chiếu.
     *
     * @param request thông tin lịch chiếu cần tạo
     * @return lịch chiếu vừa được tạo
     */
    @PostMapping
    public ApiResponse<ShowtimeResponse> createShowtime(@Valid @RequestBody ShowtimeRequest request) {
        ShowtimeResponse response = showtimeService.createShowtime(request);
        return new ApiResponse.Builder<ShowtimeResponse>()
                .success(true)
                .message("Tạo lịch chiếu thành công")
                .data(response)
                .build();
    }
}
