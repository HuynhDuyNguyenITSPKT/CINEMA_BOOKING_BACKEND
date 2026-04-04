package com.movie.cinema_booking_backend.service.movie.factory;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.entity.Showtime;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;

import java.time.LocalDateTime;

public interface IShowtimeFactory {

    Showtime createEntity(Movie movie, Auditorium auditorium,
                          LocalDateTime startTime, LocalDateTime endTime,
                          int basePrice);

    ShowtimeResponse createResponse(Showtime showtime);
}
