package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.GenreRepository;
import com.movie.cinema_booking_backend.repository.MovieRepository;
import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IMovieService;
import com.movie.cinema_booking_backend.service.movie.factory.IMovieFactory;
import com.movie.cinema_booking_backend.service.movie.observer.MovieEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * MovieService - Triển khai business logic cho Movie module.
 *
 * Luồng xử lý chuẩn:
 *   Request → validate → Factory (build entity) → Repository (persist) → Observer (notify) → Response
 *
 * SOLID — Single Responsibility: Service chỉ điều phối, không build entity thủ công.
 * SOLID — Open/Closed: Thêm observer mới không cần sửa service.
 * SOLID — Dependency Inversion: Phụ thuộc vào IMovieFactory, không MovieFactory cụ thể.
 *
 * Design Patterns:
 * - Factory (IMovieFactory): tạo/map Movie entity và DTO
 * - Observer (MovieEventPublisher): thông báo sự kiện CRUD phim
 */
@Service
@RequiredArgsConstructor
public class MovieService implements IMovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final IMovieFactory movieFactory;
    private final MovieEventPublisher movieEventPublisher;

    @Override
    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        if (movieRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.MOVIE_TITLE_EXISTS);
        }

        // Factory chịu trách nhiệm build entity — Service chỉ điều phối (SRP)
        List<Genre> genres = resolveGenres(request.getGenreIds());
        Movie movie = movieFactory.createEntity(request, genres);
        movie = movieRepository.save(movie);

        MovieResponse response = movieFactory.createResponse(movie);
        // Observer Pattern: thông báo tất cả subscribers về phim mới
        movieEventPublisher.notifyMovieAdded(response);
        return response;
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(String id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        // Chỉ kiểm tra trùng tên nếu tên thực sự thay đổi
        if (!movie.getTitle().equals(request.getTitle())
                && movieRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.MOVIE_TITLE_EXISTS);
        }

        // Factory chịu trách nhiệm update fields — không set thủ công từng field ở Service
        List<Genre> genres = resolveGenres(request.getGenreIds());
        movieFactory.updateEntity(movie, request, genres);
        movie = movieRepository.save(movie);

        MovieResponse response = movieFactory.createResponse(movie);
        // Observer Pattern: thông báo tất cả subscribers về phim được cập nhật
        movieEventPublisher.notifyMovieUpdated(response);
        return response;
    }

    @Override
    @Transactional
    public void deleteMovie(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        // Xóa bidirectional relationship trước khi delete entity
        movie.removeAllGenres();
        movieRepository.delete(movie);

        // Observer Pattern: thông báo tất cả subscribers về phim bị xóa
        movieEventPublisher.notifyMovieDeleted(id);
    }

    @Override
    public MovieResponse getMovieById(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        return movieFactory.createResponse(movie);
    }

    @Override
    public Page<MovieResponse> getAllMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAll(pageable).map(movieFactory::createResponse);
    }

    /**
     * Tìm kiếm và lọc phim đang chiếu (NOW_SHOWING) theo keyword và thể loại.
     * Được gọi bởi PublicCinemaFacade — Facade không inject repository trực tiếp.
     *
     * SOLID — DIP: Facade gọi qua interface này thay vì MovieRepository.
     */
    @Override
    public PaginationResponse<MovieResponse> searchNowShowingMovies(
            String keyword, String genreId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviePage = movieRepository.searchAndFilterShowingMovies(
                keyword, genreId, MovieStatus.NOW_SHOWING, pageable);

        List<MovieResponse> items = moviePage.getContent().stream()
                .map(movieFactory::createResponse)
                .toList();

        return PaginationResponse.<MovieResponse>builder()
                .currentItems(items)
                .totalItems(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .currentPage(moviePage.getNumber())
                .build();
    }

    // ================================================================
    // Private Helpers
    // ================================================================

    /**
     * Validate và resolve Genre entities từ danh sách ID.
     * Private helper — tái sử dụng ở createMovie và updateMovie (DRY).
     *
     * @param genreIds danh sách ID genre từ request
     * @return danh sách Genre entity đã verify từ DB
     * @throws AppException nếu có ID không tồn tại trong DB
     */
    private List<Genre> resolveGenres(List<String> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size()) {
            throw new AppException(ErrorCode.GENRE_NOT_FOUND);
        }
        return genres;
    }
}
