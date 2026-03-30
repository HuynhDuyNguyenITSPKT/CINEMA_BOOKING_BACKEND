package com.movie.cinema_booking_backend.service.genre.factory.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.service.genre.factory.IGenreFactory;
import org.springframework.stereotype.Component;

/**
 * GenreFactory - Factory Pattern Implementation cho Genre.
 *
 * Tập trung toàn bộ mapping logic tại đây.
 * Service GenreService chỉ gọi các method này, không tự build entity.
 *
 * SOLID — Single Responsibility: Chỉ chịu trách nhiệm tạo/map Genre objects.
 * OOP — Encapsulation: Logic khởi tạo được đóng gói, Service không cần biết chi tiết.
 */
@Component
public class GenreFactory implements IGenreFactory {

    /**
     * Tạo Genre Entity mới từ request.
     * Validate null trước khi build để fail-fast thay vì NullPointerException sau đó.
     *
     * @param request dữ liệu genre từ client
     * @return genre entity (chưa save)
     */
    @Override
    public Genre createGenreEntity(GenreRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("GenreRequest không thể null");
        }
        return Genre.builder()
                .name(request.getName().trim())
                .build();
    }

    /**
     * Cập nhật Genre Entity hiện có từ request.
     * Factory chịu trách nhiệm mapping — Service không set thủ công từng field (SRP).
     *
     * @param genre   genre entity hiện tại (mutable)
     * @param request dữ liệu cập nhật từ client
     */
    @Override
    public void updateGenreEntity(Genre genre, GenreRequest request) {
        if (genre == null) {
            throw new IllegalArgumentException("Genre không thể null");
        }
        if (request == null) {
            throw new IllegalArgumentException("GenreRequest không thể null");
        }
        genre.setName(request.getName().trim());
    }

    /**
     * Map Genre Entity sang GenreResponse DTO.
     * Validate null để đảm bảo không trả về null response cho client.
     *
     * @param genre genre entity từ DB
     * @return genre DTO an toàn trả về client
     */
    @Override
    public GenreResponse createGenreResponse(Genre genre) {
        if (genre == null) {
            throw new IllegalArgumentException("Genre không thể null");
        }
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}
