package com.movie.cinema_booking_backend.service.movie.observer.impl;

import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.service.movie.observer.IMovieObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserNotificationObserver - Observer đảm nhận gửi thông báo cho người dùng.
 *
 * Nhận tín hiệu (event) khi có thay đổi từ hệ thống phim.
 */
@Slf4j
@Component
public class UserNotificationObserver implements IMovieObserver {

    @Override
    public void onMovieAdded(MovieResponse movie) {
        log.info("[UserNotificationObserver] Phim mới: {} — chuẩn bị gửi thông báo tới users.", movie.getTitle());
        log.debug("[UserNotificationObserver] Đang lên lịch gửi Email / Push Notification tới khách hàng thân thiết.");
        // TODO: Tích hợp Firebase/Email service — gửi push notification "Phim mới ra mắt!"
    }

    @Override
    public void onMovieUpdated(MovieResponse movie) {
        log.info("[UserNotificationObserver] Phim được cập nhật: {}", movie.getTitle());
        // TODO: Nếu movie.getStatus() == NOW_SHOWING → gửi push "Mở bán vé!" tới users đã bookmark phim này
    }

    @Override
    public void onMovieDeleted(String movieId) {
        log.info("[UserNotificationObserver] Phim bị xóa — ID: {}", movieId);
        log.debug("[UserNotificationObserver] Đang gửi thông báo tới users về phim bị gỡ khỏi lịch chiếu.");
        // TODO: Gửi notification tới users đã đặt vé/bookmark phim này
    }
}
