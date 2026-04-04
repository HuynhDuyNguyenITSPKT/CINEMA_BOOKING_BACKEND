package com.movie.cinema_booking_backend.service.movie.builder;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.request.MovieRequest;

import java.util.List;

public interface IMovieBuilder {

    IMovieBuilder withBasicInfo(MovieRequest request);

    IMovieBuilder withGenres(List<Genre> genres);

    Movie build();
}
