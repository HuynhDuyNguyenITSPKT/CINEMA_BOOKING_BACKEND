package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.AdminMovieReviewRequest;
import com.movie.cinema_booking_backend.request.MovieReviewUpdateRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.MovieReviewResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IMovieReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Validated
public class AdminMovieReviewController {

    private final IMovieReviewService movieReviewService;

    @GetMapping
    public ApiResponse<PaginationResponse<MovieReviewResponse>> getReviewsForAdmin(
            @RequestParam(required = false) String movieId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<MovieReviewResponse> pageResult = movieReviewService.getReviewsForAdmin(
                movieId,
                minRating,
                maxRating,
                fromDate,
                toDate,
                keyword,
                sortBy,
                sortDir,
                page,
                size
        );

        PaginationResponse<MovieReviewResponse> pagination = PaginationResponse.<MovieReviewResponse>builder()
                .currentItems(pageResult.getContent())
                .totalItems(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .build();

        return ApiResponse.<PaginationResponse<MovieReviewResponse>>builder()
                .success(true)
                .message("Lấy danh sách bình luận thành công")
                .data(pagination)
                .build();
    }

    @PostMapping
    public ApiResponse<MovieReviewResponse> createReviewByAdmin(@Valid @RequestBody AdminMovieReviewRequest request) {
        return ApiResponse.<MovieReviewResponse>builder()
                .success(true)
                .message("Tạo bình luận thành công")
                .data(movieReviewService.createReviewByAdmin(request))
                .build();
    }

    @PutMapping("/{reviewId}")
    public ApiResponse<MovieReviewResponse> updateReviewByAdmin(
            @PathVariable String reviewId,
            @Valid @RequestBody MovieReviewUpdateRequest request
    ) {
        return ApiResponse.<MovieReviewResponse>builder()
                .success(true)
                .message("Cập nhật bình luận thành công")
                .data(movieReviewService.updateReviewByAdmin(reviewId, request))
                .build();
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReviewByAdmin(@PathVariable String reviewId) {
        movieReviewService.deleteReviewByAdmin(reviewId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa bình luận thành công")
                .build();
    }
}
