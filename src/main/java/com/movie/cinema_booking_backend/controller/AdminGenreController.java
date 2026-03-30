package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IGenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * AdminGenreController — Quản lý CRUD thể loại phim (Admin only).
 *
 * <p>Tất cả endpoints được bảo vệ bởi role ADMIN thông qua SecurityConfig.
 * Controller chỉ nhận request, delegate sang service, trả về response — không chứa business logic.
 *
 * <p>Base path: {@code /api/admin/genres}
 */
@RestController
@RequestMapping("/api/admin/genres")
@RequiredArgsConstructor
public class AdminGenreController {

    private final IGenreService genreService;

    /**
     * Lấy thông tin chi tiết một thể loại theo ID.
     *
     * @param id ID thể loại cần tra cứu
     * @return thông tin thể loại
     */
    @GetMapping("/{id}")
    public ApiResponse<GenreResponse> getGenreById(@PathVariable String id) {
        return new ApiResponse.Builder<GenreResponse>()
                .success(true)
                .message("Lấy thông tin thể loại thành công")
                .data(genreService.getGenreById(id))
                .build();
    }

    /**
     * Lấy danh sách thể loại phân trang — dành cho trang quản lý admin.
     *
     * @param page trang hiện tại (0-indexed, mặc định 0)
     * @param size số item mỗi trang (mặc định 10)
     * @return danh sách thể loại có phân trang
     */
    @GetMapping
    public ApiResponse<PaginationResponse<GenreResponse>> getAllGenresPageable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<GenreResponse> pageResult = genreService.getAllGenres(page, size);

        PaginationResponse<GenreResponse> pagination = new PaginationResponse.Builder<GenreResponse>()
                .currentItems(pageResult.getContent())
                .totalItems(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .build();

        return new ApiResponse.Builder<PaginationResponse<GenreResponse>>()
                .success(true)
                .message("Lấy danh sách thể loại thành công")
                .data(pagination)
                .build();
    }

    /**
     * Tạo thể loại phim mới.
     *
     * <p>Validate: Tên thể loại không được trùng (case-sensitive).
     *
     * @param request tên thể loại cần tạo
     * @return xác nhận tạo thành công (không trả về data)
     */
    @PostMapping
    public ApiResponse<Void> createGenre(@Valid @RequestBody GenreRequest request) {
        genreService.createGenre(request);
        return new ApiResponse.Builder<Void>()
                .success(true)
                .message("Thêm thể loại thành công")
                .build();
    }

    /**
     * Cập nhật tên thể loại theo ID.
     *
     * <p>Validate: Thể loại tồn tại, tên mới không trùng với thể loại khác.
     *
     * @param id      ID thể loại cần cập nhật
     * @param request dữ liệu cập nhật
     * @return thể loại sau khi cập nhật
     */
    @PutMapping("/{id}")
    public ApiResponse<GenreResponse> updateGenre(
            @PathVariable String id,
            @Valid @RequestBody GenreRequest request) {

        return new ApiResponse.Builder<GenreResponse>()
                .success(true)
                .message("Cập nhật thể loại thành công")
                .data(genreService.updateGenre(id, request))
                .build();
    }

    /**
     * Xóa thể loại theo ID.
     *
     * <p>Validate: Thể loại phải tồn tại trước khi xóa.
     *
     * @param id ID thể loại cần xóa
     * @return xác nhận xóa thành công (không trả về data)
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGenre(@PathVariable String id) {
        genreService.deleteGenre(id);
        return new ApiResponse.Builder<Void>()
                .success(true)
                .message("Xóa thể loại thành công")
                .build();
    }
}
