package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.ShowtimeRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.IShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final IShowtimeService showtimeService;

    @PostMapping
    public ApiResponse<ShowtimeResponse> createShowtime(@Valid @RequestBody ShowtimeRequest request) {
        ShowtimeResponse response = showtimeService.createShowtime(request);
        return ApiResponse.<ShowtimeResponse>builder()
                .success(true)
                .message("Tạo lịch chiếu thành công")
                .data(response)
                .build();
    }
}
