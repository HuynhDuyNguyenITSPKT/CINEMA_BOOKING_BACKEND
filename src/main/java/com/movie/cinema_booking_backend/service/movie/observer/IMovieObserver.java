package com.movie.cinema_booking_backend.service.movie.observer;

import com.movie.cinema_booking_backend.response.MovieResponse;

public interface IMovieObserver {

    void onMovieAdded(MovieResponse movie);

    void onMovieUpdated(MovieResponse movie);

    void onMovieDeleted(String movieId);
}
