package com.movie.cinema_booking_backend.service.movie.observer;

import com.movie.cinema_booking_backend.response.MovieResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovieEventPublisher {

    private final List<IMovieObserver> observers;

    public MovieEventPublisher(List<IMovieObserver> observers) {
        this.observers = observers;
    }

    public void notifyMovieAdded(MovieResponse movie) {
        observers.forEach(observer -> observer.onMovieAdded(movie));
    }

    public void notifyMovieUpdated(MovieResponse movie) {
        observers.forEach(observer -> observer.onMovieUpdated(movie));
    }

    public void notifyMovieDeleted(String movieId) {
        observers.forEach(observer -> observer.onMovieDeleted(movieId));
    }
}
