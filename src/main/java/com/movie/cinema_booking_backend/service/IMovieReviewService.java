package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.AdminMovieReviewRequest;
import com.movie.cinema_booking_backend.request.MovieReviewRequest;
import com.movie.cinema_booking_backend.request.MovieReviewUpdateRequest;
import com.movie.cinema_booking_backend.response.MovieRatingStatsResponse;
import com.movie.cinema_booking_backend.response.MovieReviewResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface IMovieReviewService {

    MovieReviewResponse createReview(String username, MovieReviewRequest request);

    MovieReviewResponse updateMyReview(String reviewId, String username, MovieReviewUpdateRequest request);

    void deleteMyReview(String reviewId, String username);

    Page<MovieReviewResponse> getMovieReviewsForAuthenticated(
            String movieId,
            Double minRating,
            Double maxRating,
            String sortBy,
            String sortDir,
            int page,
            int size
    );

    Page<MovieReviewResponse> getMyReviews(
            String username,
            String movieId,
            String sortBy,
            String sortDir,
            int page,
            int size
    );

    MovieRatingStatsResponse getMovieRatingStats(String movieId);

    MovieReviewResponse createReviewByAdmin(AdminMovieReviewRequest request);

    MovieReviewResponse updateReviewByAdmin(String reviewId, MovieReviewUpdateRequest request);

    void deleteReviewByAdmin(String reviewId);

    Page<MovieReviewResponse> getReviewsForAdmin(
            String movieId,
            Double minRating,
            Double maxRating,
            LocalDate fromDate,
            LocalDate toDate,
            String keyword,
            String sortBy,
            String sortDir,
            int page,
            int size
    );
}
