package com.movie.cinema_booking_backend.service.movie.observer.impl;

import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.service.movie.observer.IMovieObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserNotificationObserver implements IMovieObserver {

    @Override
    public void onMovieAdded(MovieResponse movie) {
        log.info("[UserNotificationObserver] Phim mới: {} — chuẩn bị gửi thông báo tới users.", movie.getTitle());
        log.debug("[UserNotificationObserver] Đang lên lịch gửi Email / Push Notification tới khách hàng thân thiết.");
    }

    @Override
    public void onMovieUpdated(MovieResponse movie) {
        log.info("[UserNotificationObserver] Phim được cập nhật: {}", movie.getTitle());
    }

    @Override
    public void onMovieDeleted(String movieId) {
        log.info("[UserNotificationObserver] Phim bị xóa — ID: {}", movieId);
        log.debug("[UserNotificationObserver] Đang gửi thông báo tới users về phim bị gỡ khỏi lịch chiếu.");
    }
}
