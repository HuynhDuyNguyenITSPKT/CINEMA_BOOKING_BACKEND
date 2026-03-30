package com.movie.cinema_booking_backend.service.movie.factory.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.service.genre.factory.IGenreFactory;
import com.movie.cinema_booking_backend.service.movie.factory.IMovieFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MovieFactory - Factory Pattern Implementation cho Movie.
 *
 * Tập trung toàn bộ mapping logic tại đây.
 * Service MovieService chỉ gọi các method này, không tự build entity bằng tay.
 *
 * SOLID — Single Responsibility: Chỉ chịu trách nhiệm tạo/map Movie objects.
 * SOLID — Dependency Inversion: Phụ thuộc vào IGenreFactory (interface), không GenreFactory.
 * OOP — Encapsulation: Logic khởi tạo được đóng gói hoàn toàn tại đây.
 */
@Component
@RequiredArgsConstructor
public class MovieFactory implements IMovieFactory {

    private final IGenreFactory genreFactory;

    /**
     * Tạo Movie Entity mới từ request và genres đã resolve từ DB.
     * Service chỉ gọi method này, không tự new Movie() bằng tay.
     *
     * @param request movie data từ client
     * @param genres  danh sách genres đã verify từ DB
     * @return movie entity (chưa save)
     */
    @Override
    public Movie createEntity(MovieRequest request, List<Genre> genres) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .director(request.getDirector())
                .cast(request.getCast())
                .durationMinutes(request.getDurationMinutes())
                .releaseDate(request.getReleaseDate())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .ageRating(request.getAgeRating())
                .status(request.getStatus())
                .genres(new ArrayList<>())
                .build();

        // Dùng addGenre() để duy trì bidirectional relationship đúng cách
        genres.forEach(movie::addGenre);
        return movie;
    }

    /**
     * Cập nhật Movie Entity hiện có từ request và genres mới.
     * Factory chịu trách nhiệm mapping — Service không set thủ công từng field.
     *
     * OOP: Dùng removeAllGenres() để dọn sạch bidirectional refs trước khi add mới.
     * Tránh trường hợp genre list cũ vẫn còn back-reference trỏ về movie sau khi clear.
     *
     * @param movie   movie entity hiện tại (mutable)
     * @param request dữ liệu cập nhật
     * @param genres  danh sách genres mới đã verify từ DB
     */
    @Override
    public void updateEntity(Movie movie, MovieRequest request, List<Genre> genres) {
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setAgeRating(request.getAgeRating());
        movie.setStatus(request.getStatus());

        // Dùng removeAllGenres() thay vì getGenres().clear() để xóa đúng cả back-reference
        movie.removeAllGenres();
        genres.forEach(movie::addGenre);
    }

    /**
     * Map Movie Entity sang MovieResponse DTO để trả về client.
     * Mapping entities → DTOs tại đây, không ở controller hay service.
     *
     * @param movie movie entity từ DB
     * @return movie DTO an toàn trả về client
     */
    @Override
    public MovieResponse createResponse(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie không thể null");
        }

        List<GenreResponse> genreResponses = movie.getGenres().stream()
                .map(genreFactory::createGenreResponse)
                .toList();

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .durationMinutes(movie.getDurationMinutes())
                .releaseDate(movie.getReleaseDate())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .ageRating(movie.getAgeRating())
                .status(movie.getStatus())
                .genres(genreResponses)
                .build();
    }
}
