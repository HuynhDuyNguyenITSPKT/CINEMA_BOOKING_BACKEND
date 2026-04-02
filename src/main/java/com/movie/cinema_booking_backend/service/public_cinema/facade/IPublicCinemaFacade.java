package com.movie.cinema_booking_backend.service.public_cinema.facade;

import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;

import java.time.LocalDate;
import java.util.List;

public interface IPublicCinemaFacade {

    PaginationResponse<MovieResponse> searchAndFilterMovies(String keyword, String genreId, int page, int size);

    List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date);
}
