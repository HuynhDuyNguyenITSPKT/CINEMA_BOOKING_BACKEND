package com.movie.cinema_booking_backend.service.movie.observer;

import com.movie.cinema_booking_backend.response.MovieResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MovieEventPublisher — Subject (Publisher) trong Observer Pattern.
 *
 * <p>Quản lý danh sách các {@link IMovieObserver} và phát sự kiện tới tất cả
 * observers khi có thay đổi dữ liệu phim (tạo mới / cập nhật / xóa).
 *
 * <p>Spring tự động inject tất cả beans implement {@link IMovieObserver}
 * theo cơ chế dependency injection collection. Không cần đăng ký observer thủ công.
 *
 * <p>Luồng hoạt động:
 * <ol>
 *   <li>MovieService hoàn tất thao tác CRUD</li>
 *   <li>MovieService gọi phương thức notify tương ứng trên publisher</li>
 *   <li>Publisher duyệt qua toàn bộ observer và gọi callback</li>
 *   <li>Mỗi observer thực thi logic riêng (cache, notification, v.v.)</li>
 * </ol>
 *
 * <p>SOLID — Open/Closed: Thêm observer mới chỉ cần tạo class implements IMovieObserver
 * với @Component — không cần sửa publisher này.
 * <p>SOLID — Dependency Inversion: Phụ thuộc vào List&lt;IMovieObserver&gt; (abstraction),
 * không phụ thuộc vào concrete observer nào.
 * <p>Design Pattern — Observer: Publisher thông báo sự kiện; observer phản ứng độc lập.
 */
@Component
public class MovieEventPublisher {

    private final List<IMovieObserver> observers;

    /**
     * Spring inject tất cả beans implement {@link IMovieObserver} vào đây.
     *
     * @param observers danh sách observer được Spring quản lý
     */
    public MovieEventPublisher(List<IMovieObserver> observers) {
        this.observers = observers;
    }

    /**
     * Phát sự kiện phim mới được thêm vào hệ thống.
     *
     * @param movie thông tin phim vừa được tạo
     */
    public void notifyMovieAdded(MovieResponse movie) {
        observers.forEach(observer -> observer.onMovieAdded(movie));
    }

    /**
     * Phát sự kiện thông tin phim được cập nhật.
     *
     * @param movie thông tin phim sau khi cập nhật
     */
    public void notifyMovieUpdated(MovieResponse movie) {
        observers.forEach(observer -> observer.onMovieUpdated(movie));
    }

    /**
     * Phát sự kiện phim bị xóa khỏi hệ thống.
     *
     * @param movieId ID của phim vừa bị xóa
     */
    public void notifyMovieDeleted(String movieId) {
        observers.forEach(observer -> observer.onMovieDeleted(movieId));
    }
}
