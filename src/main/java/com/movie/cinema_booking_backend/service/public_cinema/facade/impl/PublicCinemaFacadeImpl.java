package com.movie.cinema_booking_backend.service.public_cinema.facade.impl;

import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.IMovieService;
import com.movie.cinema_booking_backend.service.IShowtimeService;
import com.movie.cinema_booking_backend.service.public_cinema.facade.IPublicCinemaFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("!dev")
@RequiredArgsConstructor
public class PublicCinemaFacadeImpl implements IPublicCinemaFacade {

    private final IMovieService movieService;
    private final IShowtimeService showtimeService;

    @Override
    public PaginationResponse<MovieResponse> searchAndFilterMovies(String keyword, String genreId, int page, int size) {
        return movieService.searchNowShowingMovies(keyword, genreId, page, size);
    }

    @Override
    public List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date) {
        return showtimeService.getShowtimesByMovieAndDate(movieId, date);
    }
}
