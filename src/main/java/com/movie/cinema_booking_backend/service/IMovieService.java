package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.enums.MovieStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface IMovieService {
    // Create
    MovieResponse createMovie(MovieRequest movieRequest);

    // Read
    Page<MovieResponse> getAllMovies(int page, int size);

    MovieResponse getMovieById(String id);

    MovieResponse getMovieByTitle(String title);

    Page<MovieResponse> getMoviesByStatus(MovieStatus status, int page, int size);

    Page<MovieResponse> searchMovies(String keyword, int page, int size);

    List<MovieResponse> getMoviesByReleaseDateRange(LocalDate startDate, LocalDate endDate);

    // Update
    MovieResponse updateMovie(String id, MovieRequest movieRequest);

    // Delete
    void deleteMovie(String id);

    void deleteAllMovies();
}
