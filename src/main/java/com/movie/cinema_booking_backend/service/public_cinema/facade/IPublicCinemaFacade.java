package com.movie.cinema_booking_backend.service.public_cinema.facade;

import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * IPublicCinemaFacade — Facade Pattern Interface cho Public Cinema API.
 *
 * <p>Che giấu sự phức tạp của nhiều service bên trong (Movie, Showtime),
 * cung cấp một điểm truy cập duy nhất cho phía client (khách hàng, UI).
 *
 * <p>Tính năng bao gồm:
 * <ul>
 *   <li>Tìm kiếm và lọc phim đang chiếu</li>
 *   <li>Tra cứu lịch chiếu theo phim và ngày</li>
 * </ul>
 *
 * <p>SOLID — Single Responsibility: Chỉ cung cấp API công khai cho màn hình người dùng.
 * <p>SOLID — Dependency Inversion: Controller phụ thuộc vào interface này, không vào implementation cụ thể.
 * <p>Design Pattern — Facade: Che giấu sự phức tạp của IMovieService và IShowtimeService.
 */
public interface IPublicCinemaFacade {

    /**
     * Tìm kiếm và lọc danh sách phim đang chiếu (NOW_SHOWING).
     *
     * @param keyword từ khóa tìm kiếm theo tên phim (nullable — bỏ qua nếu null)
     * @param genreId ID thể loại để lọc (nullable — bỏ qua nếu null)
     * @param page    trang hiện tại (0-indexed)
     * @param size    số lượng phim mỗi trang
     * @return danh sách phim phân trang phù hợp với bộ lọc
     */
    PaginationResponse<MovieResponse> searchAndFilterMovies(String keyword, String genreId, int page, int size);

    /**
     * Lấy danh sách lịch chiếu của một phim trong một ngày cụ thể.
     *
     * @param movieId ID của bộ phim cần tra cứu
     * @param date    ngày cần xem lịch chiếu
     * @return danh sách lịch chiếu được sắp xếp theo giờ tăng dần
     */
    List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date);
}
