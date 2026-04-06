package com.movie.cinema_booking_backend.service.movie.observer.impl;

import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.service.movie.observer.IMovieObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheUpdateObserver implements IMovieObserver {

    @Override
    public void onMovieAdded(MovieResponse movie) {
        log.info("[CacheUpdateObserver] Phim mới được thêm: {}", movie.getTitle());
    }
}
