package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.ShowtimeRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.IShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public ApiResponse<ShowtimeResponse> getShowtimeById(@PathVariable String id) {
        ShowtimeResponse response = showtimeService.getShowtimeById(id);
        return ApiResponse.<ShowtimeResponse>builder()
                .success(true)
                .message("Lấy thông tin lịch chiếu thành công")
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<ShowtimeResponse>> getAllShowtimes() {
        List<ShowtimeResponse> response = showtimeService.getAllShowtimes();
        return ApiResponse.<List<ShowtimeResponse>>builder()
                .success(true)
                .message("Lấy danh sách lịch chiếu thành công")
                .data(response)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ShowtimeResponse> updateShowtime(
            @PathVariable String id,
            @Valid @RequestBody ShowtimeRequest request) {
        ShowtimeResponse response = showtimeService.updateShowtime(id, request);
        return ApiResponse.<ShowtimeResponse>builder()
                .success(true)
                .message("Cập nhật lịch chiếu thành công")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteShowtime(@PathVariable String id) {
        showtimeService.deleteShowtime(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa lịch chiếu thành công")
                .build();
    }
}
