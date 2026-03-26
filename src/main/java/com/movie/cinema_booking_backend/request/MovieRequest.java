package com.movie.cinema_booking_backend.request;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MovieRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;

    private String description;

    @NotBlank(message = "Director cannot be blank")
    private String director;

    private String cast;

    @Positive(message = "Duration must be greater than 0")
    private int durationMinutes;

    @NotNull(message = "Release date cannot be null")
    private LocalDate releaseDate;

    private String posterUrl;

    private String trailerUrl;

    @NotBlank(message = "Age rating cannot be blank")
    private String ageRating;

    @NotNull(message = "Status cannot be null")
    private MovieStatus status;

    private List<String> genreIds;
}
