package com.movie.cinema_booking_backend.service.movie.observer;

import com.movie.cinema_booking_backend.response.MovieResponse;

/**
 * IMovieObserver - Observer Pattern Interface
 * 
 * Các observers theo dõi sự kiện phim và tự động phản ứng.
 * 
 * Implementations:
 * - CacheUpdateObserver: Xóa cache khi phim thay đổi
 * - UserNotificationObserver: Gửi notification tới users
 * 
 * SOLID: Single Responsibility - Mỗi observer chỉ handle 1 mục đích
 * SOLID: Open/Closed - Dễ thêm observer mới mà không sửa existing code
 * SOLID: Dependency Inversion - MovieEventPublisher phụ thuộc vào interface, không concrete classes
 * 
 * Benefit:
 * - Decoupled: Movie service không cần biết về cache/notification
 * - Extensible: Thêm feature mới qua observer mới
 * - Maintainable: Sửa logic observer không ảnh hưởng service
 */
public interface IMovieObserver {
    /**
     * Gọi khi phim được thêm mới
     * @param movie movie response của phim vừa tạo
     */
    void onMovieAdded(MovieResponse movie);

    /**
     * Gọi khi phim được cập nhật
     * @param movie movie response của phim vừa update
     */
    void onMovieUpdated(MovieResponse movie);

    /**
     * Gọi khi phim bị xóa
     * @param movieId ID của phim vừa xóa
     */
    void onMovieDeleted(String movieId);
}
