package com.movie.cinema_booking_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieReviewResponse {
    private String id;
    private Double rating;
    private String comment;
    private LocalDate createdAt;

    private String movieId;
    private String movieTitle;

    private Long userId;
    private String userFullName;
}
