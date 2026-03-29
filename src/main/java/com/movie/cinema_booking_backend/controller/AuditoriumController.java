package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.AuditoriumRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.service.IAuditoriumService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
public class AuditoriumController {

    private final IAuditoriumService auditoriumService;

    public AuditoriumController(IAuditoriumService auditoriumService) {
        this.auditoriumService = auditoriumService;
    }

    @GetMapping("/auditoriums")
    public ApiResponse<?> getAllAuditoriums() {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy danh sách phòng chiếu thành công")
                .data(auditoriumService.getAllAuditoriums())
                .build();
    }

    @GetMapping("/auditoriums/{id}")
    public ApiResponse<?> getAuditoriumById(@PathVariable String id) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy thông tin phòng chiếu thành công")
                .data(auditoriumService.getAuditoriumById(id))
                .build();
    }

    @PostMapping("/admin/auditoriums")
    public ApiResponse<?> createAuditorium(@Valid @RequestBody AuditoriumRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Tạo phòng chiếu thành công")
                .data(auditoriumService.createAuditorium(request))
                .build();
    }

    @PutMapping("/admin/auditoriums/{id}")
    public ApiResponse<?> updateAuditorium(@PathVariable String id,
                                           @Valid @RequestBody AuditoriumRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Cập nhật phòng chiếu thành công")
                .data(auditoriumService.updateAuditorium(id, request))
                .build();
    }

    @DeleteMapping("/admin/auditoriums/{id}")
    public ApiResponse<?> deleteAuditorium(@PathVariable String id) {
        auditoriumService.deleteAuditorium(id);
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Xoá phòng chiếu thành công")
                .build();
    }
}
