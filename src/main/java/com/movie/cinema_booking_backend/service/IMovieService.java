package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import org.springframework.data.domain.Page;

/**
 * IMovieService - Contract cho toàn bộ business logic của Movie.
 *
 * SOLID — Interface Segregation: Chỉ khai báo operations liên quan đến Movie.
 * SOLID — Dependency Inversion: Controller và Facade phụ thuộc vào interface này,
 *         không phụ thuộc trực tiếp vào MovieRepository.
 */
public interface IMovieService {

    /**
     * Tạo phim mới. Validate trùng tên và genres hợp lệ.
     * Thông báo Observer sau khi tạo thành công.
     *
     * @param request dữ liệu phim từ client
     * @return MovieResponse của phim vừa tạo
     */
    MovieResponse createMovie(MovieRequest request);

    /**
     * Cập nhật phim theo ID. Validate tồn tại, trùng tên và genres hợp lệ.
     * Thông báo Observer sau khi update thành công.
     *
     * @param id      ID phim cần cập nhật
     * @param request dữ liệu cập nhật
     * @return MovieResponse sau khi update
     */
    MovieResponse updateMovie(String id, MovieRequest request);

    /**
     * Xóa phim theo ID. Thông báo Observer sau khi xóa.
     *
     * @param id ID phim cần xóa
     */
    void deleteMovie(String id);

    /**
     * Lấy thông tin chi tiết phim theo ID.
     *
     * @param id ID phim
     * @return MovieResponse chứa thông tin phim
     */
    MovieResponse getMovieById(String id);

    /**
     * Lấy tất cả phim phân trang (admin — không lọc status).
     *
     * @param page trang hiện tại (0-indexed)
     * @param size số items mỗi trang
     * @return page chứa danh sách MovieResponse
     */
    Page<MovieResponse> getAllMovies(int page, int size);

    /**
     * Tìm kiếm và lọc phim đang chiếu (NOW_SHOWING) theo keyword và thể loại.
     * Dùng bởi PublicCinemaFacade để phục vụ public API.
     *
     * SOLID — DIP: Facade gọi qua interface này thay vì trực tiếp MovieRepository.
     *
     * @param keyword từ khóa tìm kiếm theo tên phim (nullable)
     * @param genreId ID thể loại để lọc (nullable)
     * @param page    trang hiện tại
     * @param size    số items mỗi trang
     * @return PaginationResponse chứa danh sách phim đang chiếu
     */
    PaginationResponse<MovieResponse> searchNowShowingMovies(String keyword, String genreId, int page, int size);
}
