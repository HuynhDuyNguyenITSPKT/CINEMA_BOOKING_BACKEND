package com.movie.cinema_booking_backend.service.genre.factory.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.service.genre.factory.IGenreFactory;
import org.springframework.stereotype.Component;

/**
 * GenreFactory — Factory Pattern Implementation cho Genre.
 *
 * Tập trung toàn bộ mapping logic tại đây.
 * Service GenreService chỉ gọi các method này, không tự build entity bằng tay.
 *
 * SOLID — Single Responsibility: Chỉ chịu trách nhiệm tạo/map Genre objects.
 * OOP — Encapsulation: Logic khởi tạo được đóng gói hoàn toàn tại đây.
 *
 * Null safety: Không cần null check ở đây vì:
 * - request: đã được @Valid + @NotBlank chặn từ Controller
 * - genre: Service luôn dùng orElseThrow() trước khi gọi Factory
 */
@Component
public class GenreFactory implements IGenreFactory {

    /**
     * Tạo Genre Entity mới từ request.
     * Service chỉ gọi method này, không new Genre() thủ công.
     *
     * @param request dữ liệu genre từ client (đã validate bởi @Valid)
     * @return genre entity (chưa save)
     */
    @Override
    public Genre createGenreEntity(GenreRequest request) {
        return Genre.builder()
                .name(request.getName().trim())
                .build();
    }

    /**
     * Cập nhật Genre Entity hiện có từ request.
     * Dùng setter vì genre là JPA managed entity — không thể build lại từ đầu.
     *
     * @param genre   genre entity hiện tại (managed state)
     * @param request dữ liệu cập nhật (đã validate bởi @Valid)
     */
    @Override
    public void updateGenreEntity(Genre genre, GenreRequest request) {
        genre.setName(request.getName().trim());
    }

    /**
     * Map Genre Entity sang GenreResponse DTO để trả về client.
     *
     * @param genre genre entity từ DB
     * @return genre DTO an toàn trả về client
     */
    @Override
    public GenreResponse createGenreResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}
