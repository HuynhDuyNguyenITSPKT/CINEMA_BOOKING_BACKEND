package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IGenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/genres")
@RequiredArgsConstructor
public class AdminGenreController {

    private final IGenreService genreService;

    @GetMapping("/{id}")
    public ApiResponse<GenreResponse> getGenreById(@PathVariable String id) {
        return ApiResponse.<GenreResponse>builder()
                .success(true)
                .message("Lấy thông tin thể loại thành công")
                .data(genreService.getGenreById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<PaginationResponse<GenreResponse>> getAllGenresPageable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<GenreResponse> pageResult = genreService.getAllGenres(page, size);

        PaginationResponse<GenreResponse> pagination = PaginationResponse.<GenreResponse>builder()
                .currentItems(pageResult.getContent())
                .totalItems(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .build();

        return ApiResponse.<PaginationResponse<GenreResponse>>builder()
                .success(true)
                .message("Lấy danh sách thể loại thành công")
                .data(pagination)
                .build();
    }

    @PostMapping
    public ApiResponse<Void> createGenre(@Valid @RequestBody GenreRequest request) {
        genreService.createGenre(request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Thêm thể loại thành công")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<GenreResponse> updateGenre(
            @PathVariable String id,
            @Valid @RequestBody GenreRequest request) {

        return ApiResponse.<GenreResponse>builder()
                .success(true)
                .message("Cập nhật thể loại thành công")
                .data(genreService.updateGenre(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGenre(@PathVariable String id) {
        genreService.deleteGenre(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa thể loại thành công")
                .build();
    }
}
