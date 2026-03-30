package com.movie.cinema_booking_backend.service.public_cinema.facade.impl;

import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.IMovieService;
import com.movie.cinema_booking_backend.service.IShowtimeService;
import com.movie.cinema_booking_backend.service.public_cinema.facade.IPublicCinemaFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * PublicCinemaFacadeImpl — Facade Pattern Implementation cho môi trường production.
 *
 * <p>Điều phối (orchestrate) giữa IMovieService và IShowtimeService để phục vụ API công khai.
 * Lớp này chỉ gọi các service interface cấp cao — KHÔNG inject repository trực tiếp.
 *
 * <p>SOLID — Single Responsibility: Chỉ điều phối, không chứa business logic.
 * <p>SOLID — Dependency Inversion: Phụ thuộc vào IMovieService, IShowtimeService (abstractions).
 * <p>Design Pattern — Facade: Đơn giản hoá interface phức tạp thành một điểm gọi duy nhất.
 *
 * @see MockPublicCinemaFacade dùng trong môi trường dev (Profile "dev")
 */
@Component
@Profile("!dev")
@RequiredArgsConstructor
public class PublicCinemaFacadeImpl implements IPublicCinemaFacade {

    private final IMovieService movieService;
    private final IShowtimeService showtimeService;

    /**
     * {@inheritDoc}
     *
     * <p>Delegates sang IMovieService.searchNowShowingMovies() — Facade không truy cập DB trực tiếp.
     */
    @Override
    public PaginationResponse<MovieResponse> searchAndFilterMovies(String keyword, String genreId, int page, int size) {
        return movieService.searchNowShowingMovies(keyword, genreId, page, size);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates sang IShowtimeService.getShowtimesByMovieAndDate() — Facade không truy cập DB trực tiếp.
     */
    @Override
    public List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date) {
        return showtimeService.getShowtimesByMovieAndDate(movieId, date);
    }
}
