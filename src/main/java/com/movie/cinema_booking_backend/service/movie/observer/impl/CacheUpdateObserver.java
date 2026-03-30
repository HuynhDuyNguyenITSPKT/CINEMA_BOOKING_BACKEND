package com.movie.cinema_booking_backend.service.movie.observer.impl;

import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.service.movie.observer.IMovieObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CacheUpdateObserver - Observer nhận nhiệm vụ xử lý caching.
 *
 * Thực thi khi nhận được tín hiệu (event) về Movie.
 */
@Slf4j
@Component
public class CacheUpdateObserver implements IMovieObserver {

    @Override
    public void onMovieAdded(MovieResponse movie) {
        log.info("[CacheUpdateObserver] Phim mới được thêm: {}", movie.getTitle());
        log.debug("[CacheUpdateObserver] Đang xóa cache danh sách phim cũ...");
        // TODO: Tích hợp Redis — gọi cacheManager.evict("movies") tại đây
        log.debug("[CacheUpdateObserver] Cache cleared — UI Movie List sẵn sàng reload dữ liệu mới.");
    }

    @Override
    public void onMovieUpdated(MovieResponse movie) {
        log.info("[CacheUpdateObserver] Phim được cập nhật: {} (id={})", movie.getTitle(), movie.getId());
        log.debug("[CacheUpdateObserver] Đang làm mới cache cho phim ID: {}", movie.getId());
        // TODO: Tích hợp Redis — gọi cacheManager.evict("movie:" + movie.getId()) tại đây
    }

    @Override
    public void onMovieDeleted(String movieId) {
        log.info("[CacheUpdateObserver] Phim bị xóa — ID: {}", movieId);
        log.debug("[CacheUpdateObserver] Đang xóa cache entry cho phim ID: {}", movieId);
        // TODO: Tích hợp Redis — gọi cacheManager.evict("movie:" + movieId) tại đây
    }
}
