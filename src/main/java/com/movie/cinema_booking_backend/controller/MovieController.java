package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IMovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class MovieController {

    private final IMovieService movieService;

    @PostMapping
    public ApiResponse<MovieResponse> createMovie(@RequestBody @Valid MovieRequest request) {
        return ApiResponse.<MovieResponse>builder()
                .success(true)
                .message("Thêm phim thành công")
                .data(movieService.createMovie(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<MovieResponse> updateMovie(@PathVariable String id, @RequestBody @Valid MovieRequest request) {
        return ApiResponse.<MovieResponse>builder()
                .success(true)
                .message("Cập nhật phim thành công")
                .data(movieService.updateMovie(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa phim thành công")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<MovieResponse> getMovieById(@PathVariable String id) {
        return ApiResponse.<MovieResponse>builder()
                .success(true)
                .message("Lấy thông tin phim thành công")
                .data(movieService.getMovieById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<PaginationResponse<MovieResponse>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MovieResponse> moviesPage = movieService.getAllMovies(page, size);

        PaginationResponse<MovieResponse> pagination = PaginationResponse.<MovieResponse>builder()
                .currentItems(moviesPage.getContent())
                .totalItems(moviesPage.getTotalElements())
                .totalPages(moviesPage.getTotalPages())
                .currentPage(moviesPage.getNumber())
                .build();

        return ApiResponse.<PaginationResponse<MovieResponse>>builder()
                .success(true)
                .message("Lấy danh sách phim thành công")
                .data(pagination)
                .build();
    }
}
