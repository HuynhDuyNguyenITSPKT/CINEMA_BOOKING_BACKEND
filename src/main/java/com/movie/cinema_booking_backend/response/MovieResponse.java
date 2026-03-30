package com.movie.cinema_booking_backend.response;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * MovieResponse — DTO trả về client cho Movie.
 *
 * OOP — Immutable: chỉ @Getter, không có setter.
 * Builder: khởi tạo qua Lombok @Builder trong MovieFactory.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponse {
    private String id;
    private String title;
    private String description;
    private String director;
    private String cast;
    private int durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private String ageRating;
    private MovieStatus status;
    private List<GenreResponse> genres;
}
