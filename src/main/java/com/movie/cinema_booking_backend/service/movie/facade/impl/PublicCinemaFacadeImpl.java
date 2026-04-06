package com.movie.cinema_booking_backend.service.movie.facade.impl;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.IMovieService;
import com.movie.cinema_booking_backend.service.IShowtimeService;
import com.movie.cinema_booking_backend.service.movie.facade.IPublicCinemaFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PublicCinemaFacadeImpl implements IPublicCinemaFacade {

    private final IMovieService movieService;
    private final IShowtimeService showtimeService;

    @Override
    public PaginationResponse<MovieResponse> searchAndFilterMovies(
            String keyword,
            String genreId,
            MovieStatus status,
            int page,
            int size) {
        return movieService.searchMovies(keyword, genreId, status, page, size);
    }

    @Override
    public MovieResponse getMovieById(String movieId) {
        return movieService.getMovieById(movieId);
    }

    @Override
    public List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date) {
        return showtimeService.getShowtimesByMovieAndDate(movieId, date);
    }
}
