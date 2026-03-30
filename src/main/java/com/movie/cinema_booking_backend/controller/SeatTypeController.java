package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.SeatTypeRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.service.ISeatTypeService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
public class SeatTypeController {

    private final ISeatTypeService seatTypeService;

    public SeatTypeController(ISeatTypeService seatTypeService) {
        this.seatTypeService = seatTypeService;
    }

    @GetMapping("/seat-types")
    public ApiResponse<?> getAllSeatTypes() {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy danh sách loại ghế thành công")
                .data(seatTypeService.getAllSeatTypes())
                .build();
    }

    @GetMapping("/seat-types/{id}")
    public ApiResponse<?> getSeatTypeById(@PathVariable String id) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy thông tin loại ghế thành công")
                .data(seatTypeService.getSeatTypeById(id))
                .build();
    }

    @PostMapping("/admin/seat-types")
    public ApiResponse<?> createSeatType(@Valid @RequestBody SeatTypeRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Tạo loại ghế thành công")
                .data(seatTypeService.createSeatType(request))
                .build();
    }

    @PutMapping("/admin/seat-types/{id}")
    public ApiResponse<?> updateSeatType(@PathVariable String id,
                                         @Valid @RequestBody SeatTypeRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Cập nhật loại ghế thành công")
                .data(seatTypeService.updateSeatType(id, request))
                .build();
    }

    @DeleteMapping("/admin/seat-types/{id}")
    public ApiResponse<?> deleteSeatType(@PathVariable String id) {
        seatTypeService.deleteSeatType(id);
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Xoá loại ghế thành công")
                .build();
    }
}
