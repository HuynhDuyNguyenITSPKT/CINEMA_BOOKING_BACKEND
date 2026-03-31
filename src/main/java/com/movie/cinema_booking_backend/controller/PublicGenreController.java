package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.service.IGenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PublicGenreController — API thể loại phim dành cho public (không yêu cầu đăng nhập).
 *
 * <p>Cung cấp danh sách thể loại không phân trang để sử dụng cho:
 * <ul>
 *   <li>Dropdown chọn thể loại khi tìm kiếm phim</li>
 *   <li>Filter theo thể loại trên màn hình khách hàng</li>
 * </ul>
 *
 * <p>Base path: {@code /api/genres}
 * <p>Security: Public — permit all (khai báo trong SecurityConfig.PUBLIC_ENDPOINTS)
 */
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class PublicGenreController {

    private final IGenreService genreService;

    /**
     * Lấy tất cả thể loại phim (không phân trang).
     *
     * <p>Dùng cho dropdown/filter ở phía client — không cần phân trang vì số lượng thể loại ít.
     *
     * @return danh sách thể loại phim
     */
    @GetMapping
    public ApiResponse<List<GenreResponse>> getAllGenres() {
        return ApiResponse.<List<GenreResponse>>builder()
                .success(true)
                .message("Lấy danh sách thể loại thành công")
                .data(genreService.getAllGenres())
                .build();
    }
}
