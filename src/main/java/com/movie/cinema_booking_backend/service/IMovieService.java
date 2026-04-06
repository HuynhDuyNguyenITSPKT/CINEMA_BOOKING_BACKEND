package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import org.springframework.data.domain.Page;

public interface IMovieService {

    MovieResponse createMovie(MovieRequest request);

    MovieResponse updateMovie(String id, MovieRequest request);

    void deleteMovie(String id);

    MovieResponse getMovieById(String id);

    Page<MovieResponse> getAllMovies(int page, int size);

    PaginationResponse<MovieResponse> searchMovies(
            String keyword,
            String genreId,
            MovieStatus status,
            int page,
            int size);
}
