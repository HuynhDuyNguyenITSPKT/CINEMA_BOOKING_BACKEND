package com.movie.cinema_booking_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRatingStatsResponse {
    private String movieId;
    private String movieTitle;
    private Double averageRating;
    private Long totalComments;
}
