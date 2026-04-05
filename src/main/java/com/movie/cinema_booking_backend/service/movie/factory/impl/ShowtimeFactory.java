package com.movie.cinema_booking_backend.service.movie.factory.impl;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.entity.Showtime;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.movie.factory.IShowtimeFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ShowtimeFactory implements IShowtimeFactory {

    @Override
    public Showtime createEntity(Movie movie, Auditorium auditorium,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 int basePrice) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie không thể null khi tạo Showtime");
        }
        if (auditorium == null) {
            throw new IllegalArgumentException("Auditorium không thể null khi tạo Showtime");
        }
        return Showtime.builder()
                .movie(movie)
                .auditorium(auditorium)
                .startTime(startTime)
                .endTime(endTime)
                .basePrice(basePrice)
                .build();
    }

    @Override
    public ShowtimeResponse createResponse(Showtime showtime) {
        if (showtime == null) {
            throw new IllegalArgumentException("Showtime không thể null");
        }
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovie().getId())
                .movieTitle(showtime.getMovie().getTitle())
                .auditoriumId(showtime.getAuditorium().getId())
                .auditoriumName(showtime.getAuditorium().getName())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .build();
    }
}
