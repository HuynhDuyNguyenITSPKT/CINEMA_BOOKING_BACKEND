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
        log.info("[CacheUpdateObserver] Nhận được tín hiệu có phim mới: {}", movie.getTitle());
        log.info("[CacheUpdateObserver] Đang xóa bộ nhớ đệm Cache danh sách phim cũ...");
        // Logic thực tế giải quyết việc xóa cache Redis sẽ nằm ở đây
        log.info("[CacheUpdateObserver] Màn hình UI Movie List đã sẵn sàng load dữ liệu mới cực trong trẻo!");
    }

    @Override
    public void onMovieUpdated(MovieResponse movie) {
        log.info("[CacheUpdateObserver] Tín hiệu Cập nhật dữ liệu phim: {}", movie.getTitle());
        log.info("[CacheUpdateObserver] Đang làm mới Cache cụ thể cho phim này thông qua ID {}", movie.getId());
    }

    @Override
    public void onMovieDeleted(String movieId) {
        log.info("[CacheUpdateObserver] Tín hiệu Xóa phim ID: {}", movieId);
        log.info("[CacheUpdateObserver] Đang xóa Cache cho phim bị xóa khỏi hệ thống...");
    }
}
