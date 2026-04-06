package com.movie.cinema_booking_backend.service.movie.facade;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;

import java.time.LocalDate;
import java.util.List;

public interface IPublicCinemaFacade {

    PaginationResponse<MovieResponse> searchAndFilterMovies(
            String keyword,
            String genreId,
            MovieStatus status,
            int page,
            int size);

    MovieResponse getMovieById(String movieId);

    List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date);
}
