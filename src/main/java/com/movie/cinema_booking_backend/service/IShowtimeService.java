package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.ShowtimeRequest;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;

import java.time.LocalDate;
import java.util.List;

public interface IShowtimeService {

    ShowtimeResponse createShowtime(ShowtimeRequest request);

    List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date);
}
