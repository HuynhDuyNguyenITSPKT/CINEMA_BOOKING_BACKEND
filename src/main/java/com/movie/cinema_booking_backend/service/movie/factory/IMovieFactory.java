package com.movie.cinema_booking_backend.service.movie.factory;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.MovieResponse;

import java.util.List;

public interface IMovieFactory {

    Movie createEntity(MovieRequest request, List<Genre> genres);

    void updateEntity(Movie movie, MovieRequest request, List<Genre> genres);

    MovieResponse createResponse(Movie movie);
}
