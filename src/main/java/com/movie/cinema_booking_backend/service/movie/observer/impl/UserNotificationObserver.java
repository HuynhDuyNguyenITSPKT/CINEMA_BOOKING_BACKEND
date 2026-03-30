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
        log.info("[UserNotificationObserver] PHÁT HIỆN SIÊU PHẨM MỚI TẠI RẠP: {}", movie.getTitle());
        log.info("[UserNotificationObserver] Cỗ máy tự động đang lên lịch gửi Email / Push Notification tới toàn bộ khách hàng thân thiết!");
    }

    @Override
    public void onMovieUpdated(MovieResponse movie) {
        // Có thể áp dụng chiến lược if(movie.getStatus() == NOW_SHOWING) -> Báo Push thông báo "Mở bán!"
        log.info("[UserNotificationObserver] Tin vui: Phim '{}' vừa có tin tức cập nhật cực hot! Hãy đón xem!", movie.getTitle());
    }

    @Override
    public void onMovieDeleted(String movieId) {
        log.info("[UserNotificationObserver] Tín hiệu xóa phim ID: {}", movieId);
        log.info("[UserNotificationObserver] Gửi thông báo đến tất cả người dùng về phim bị gỡ khỏi lịch chiếu...");
    }
}
