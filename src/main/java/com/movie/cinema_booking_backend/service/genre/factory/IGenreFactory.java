package com.movie.cinema_booking_backend.service.genre.factory;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;

/**
 * IGenreFactory - Factory Pattern Interface cho Genre.
 *
 * Tập trung toàn bộ logic khởi tạo và mapping Genre objects.
 * Service chỉ gọi factory, không tự build entity bằng tay.
 *
 * SOLID — Single Responsibility: Factory chỉ chịu trách nhiệm tạo/map objects.
 * SOLID — Dependency Inversion: Service phụ thuộc vào interface này, không concrete class.
 *
 * Benefit:
 * - Dễ test: mock factory trong unit test
 * - Dễ thay đổi: sửa mapping logic không ảnh hưởng service
 * - Clean code: Service sạch, không lộn xộn với entity building
 */
public interface IGenreFactory {

    /**
     * Tạo Genre Entity mới từ request.
     * Service chỉ gọi method này, không new Genre() thủ công.
     *
     * @param request dữ liệu genre từ client
     * @return genre entity (chưa save)
     */
    Genre createGenreEntity(GenreRequest request);

    /**
     * Cập nhật Genre Entity hiện có từ request.
     * Factory chịu trách nhiệm mapping — Service không set thủ công từng field.
     *
     * @param genre   genre entity hiện tại (mutable)
     * @param request dữ liệu cập nhật từ client
     */
    void updateGenreEntity(Genre genre, GenreRequest request);

    /**
     * Map Genre Entity sang GenreResponse DTO để trả về client.
     *
     * @param genre genre entity từ DB
     * @return genre DTO an toàn trả về client
     */
    GenreResponse createGenreResponse(Genre genre);
}
