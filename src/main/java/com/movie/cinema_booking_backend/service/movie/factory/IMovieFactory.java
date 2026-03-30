package com.movie.cinema_booking_backend.service.movie.factory;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.MovieResponse;

import java.util.List;

/**
 * IMovieFactory - Factory Pattern Interface
 * 
 * Tập trung logic khởi tạo & mapping entities:
 * - Tạo Movie entity từ request (Create)
 * - Cập nhật Movie entity từ request (Update)
 * - Map Movie entity sang MovieResponse DTO (Read)
 * 
 * SOLID: Single Responsibility - Factory chỉ chịu trách nhiệm tạo/map objects
 * SOLID: Dependency Inversion - Service phụ thuộc vào Factory interface, không trực tiếp map
 * 
 * Benefit:
 * - Dễ test: mock factory
 * - Dễ change: thay đổi logic khởi tạo không ảnh hưởng service
 * - Clean code: Service không lộn xộn với logic entity building
 */
public interface IMovieFactory {

    /**
     * Tạo Movie Entity mới từ request và danh sách Genre đã được resolve từ DB.
     * Service chỉ gọi method này, không tự build entity bằng tay.
     * 
     * @param request movie data từ client
     * @param genres danh sách genres đã verify từ DB
     * @return movie entity (chưa save)
     */
    Movie createEntity(MovieRequest request, List<Genre> genres);

    /**
     * Cập nhật Movie Entity hiện có từ request.
     * Factory chịu trách nhiệm mapping, Service không set thủ công từng field.
     * 
     * @param movie movie entity hiện tại (mutable)
     * @param request dữ liệu cập nhật
     * @param genres danh sách genres mới
     */
    void updateEntity(Movie movie, MovieRequest request, List<Genre> genres);

    /**
     * Tạo MovieResponse DTO từ Movie Entity để trả về client.
     * Mapping entities → DTOs tại đây, không ở controller hay service.
     * 
     * @param movie movie entity từ DB
     * @return movie DTO an toàn trả về client
     */
    MovieResponse createResponse(Movie movie);
}
