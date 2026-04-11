package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.MovieReviewRequest;
import com.movie.cinema_booking_backend.request.MovieReviewUpdateRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.MovieReviewResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IMovieReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
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

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Validated
public class MovieReviewController {

    private final IMovieReviewService movieReviewService;

    @PostMapping
    public ApiResponse<MovieReviewResponse> createReview(
            @Valid @RequestBody MovieReviewRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<MovieReviewResponse>builder()
                .success(true)
                .message("Đánh giá phim thành công")
                .data(movieReviewService.createReview(authentication.getName(), request))
                .build();
    }

    @GetMapping("/movies/{movieId}")
    public ApiResponse<PaginationResponse<MovieReviewResponse>> getMovieReviews(
            @PathVariable String movieId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<MovieReviewResponse> pageResult = movieReviewService.getMovieReviewsForAuthenticated(
                movieId,
                minRating,
                maxRating,
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

    @GetMapping("/my")
    public ApiResponse<PaginationResponse<MovieReviewResponse>> getMyReviews(
            Authentication authentication,
            @RequestParam(required = false) String movieId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<MovieReviewResponse> pageResult = movieReviewService.getMyReviews(
                authentication.getName(),
                movieId,
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
                .message("Lấy bình luận của tôi thành công")
                .data(pagination)
                .build();
    }

    @PutMapping("/{reviewId}")
    public ApiResponse<MovieReviewResponse> updateMyReview(
            @PathVariable String reviewId,
            @Valid @RequestBody MovieReviewUpdateRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<MovieReviewResponse>builder()
                .success(true)
                .message("Cập nhật bình luận thành công")
                .data(movieReviewService.updateMyReview(reviewId, authentication.getName(), request))
                .build();
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteMyReview(@PathVariable String reviewId, Authentication authentication) {
        movieReviewService.deleteMyReview(reviewId, authentication.getName());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa bình luận thành công")
                .build();
    }
}
