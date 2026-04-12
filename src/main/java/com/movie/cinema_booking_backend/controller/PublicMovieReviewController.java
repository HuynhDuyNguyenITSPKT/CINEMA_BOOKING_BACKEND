package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.MovieRatingStatsResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IMovieReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/cinema")
@RequiredArgsConstructor
public class PublicMovieReviewController {

    private final IMovieReviewService movieReviewService;

    @GetMapping("/movies/{movieId}/rating-stats")
    public ApiResponse<MovieRatingStatsResponse> getMovieRatingStats(@PathVariable String movieId) {
        return ApiResponse.<MovieRatingStatsResponse>builder()
                .success(true)
                .message("Lấy thống kê đánh giá phim thành công")
                .data(movieReviewService.getMovieRatingStats(movieId))
                .build();
    }

    @GetMapping("/movies/top-rated")
    public ApiResponse<PaginationResponse<MovieRatingStatsResponse>> getTopRatedMovies(
            @RequestParam(defaultValue = "1") Long minComments,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PaginationResponse<MovieRatingStatsResponse>>builder()
                .success(true)
                .message("Lấy danh sách phim đánh giá cao thành công")
                .data(movieReviewService.getTopRatedMovies(minComments, page, size))
                .build();
    }
}
