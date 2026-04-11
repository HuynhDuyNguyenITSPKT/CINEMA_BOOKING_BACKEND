package com.movie.cinema_booking_backend.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class MovieRatingStatsResponse {
    private String movieId;
    private String movieTitle;
    private Double averageRating;
    private Long totalComments;

    public MovieRatingStatsResponse(String movieId, String movieTitle, Double averageRating, Long totalComments) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.averageRating = averageRating == null ? 0.0 : averageRating;
        this.totalComments = totalComments;
    }

    public MovieRatingStatsResponse(String movieId, String movieTitle, Number averageRating, Long totalComments) {
        this(movieId, movieTitle, averageRating == null ? null : averageRating.doubleValue(), totalComments);
    }
}
