package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * IGenreService - Contract cho toàn bộ business logic của Genre.
 *
 * SOLID — Interface Segregation: Chỉ khai báo các operation liên quan đến Genre.
 * SOLID — Dependency Inversion: Controller và Facade phụ thuộc vào interface này.
 */
public interface IGenreService {

    /**
     * Lấy tất cả genre phân trang (dùng cho admin quản lý).
     *
     * @param page trang hiện tại (0-indexed)
     * @param size số items mỗi trang
     * @return page chứa danh sách GenreResponse
     */
    Page<GenreResponse> getAllGenres(int page, int size);

    /**
     * Lấy tất cả genre (không phân trang) dùng cho dropdown, filter.
     *
     * @return danh sách tất cả GenreResponse
     */
    List<GenreResponse> getAllGenres();

    /**
     * Lấy thông tin chi tiết một thể loại theo ID.
     *
     * @param id ID thể loại cần tra cứu
     * @return GenreResponse chứa thông tin thể loại
     */
    GenreResponse getGenreById(String id);

    /**
     * Tạo thể loại mới. Validate trùng tên trước khi lưu.
     *
     * @param request dữ liệu thể loại từ client
     */
    void createGenre(GenreRequest request);

    /**
     * Cập nhật tên thể loại. Validate tồn tại và trùng tên.
     *
     * @param id      ID thể loại cần cập nhật
     * @param request dữ liệu cập nhật
     * @return GenreResponse sau khi update
     */
    GenreResponse updateGenre(String id, GenreRequest request);

    /**
     * Xóa thể loại theo ID. Validate tồn tại trước khi xóa.
     *
     * @param id ID thể loại cần xóa
     */
    void deleteGenre(String id);
}
