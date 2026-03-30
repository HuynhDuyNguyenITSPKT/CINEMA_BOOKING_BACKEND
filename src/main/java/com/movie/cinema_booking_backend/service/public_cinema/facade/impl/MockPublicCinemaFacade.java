package com.movie.cinema_booking_backend.service.public_cinema.facade.impl;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.public_cinema.facade.IPublicCinemaFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * MockPublicCinemaFacade — Facade Pattern Implementation cho môi trường dev.
 *
 * <p>Trả về dữ liệu giả định (mock data) thay vì gọi DB thực tế.
 * Cho phép frontend/team khác phát triển độc lập khi DB chưa sẵn sàng.
 *
 * <p>Design Pattern — Facade: Cùng interface IPublicCinemaFacade, chỉ khác implementation.
 * <p>Spring Profile: Chỉ được kích hoạt khi chạy với profile {@code dev}.
 *
 * @see PublicCinemaFacadeImpl dùng trong môi trường production
 */
@Slf4j
@Component
@Profile("dev")
public class MockPublicCinemaFacade implements IPublicCinemaFacade {

    /**
     * {@inheritDoc}
     *
     * <p>Trả về một phim mock cố định thay vì query DB.
     */
    @Override
    public PaginationResponse<MovieResponse> searchAndFilterMovies(String keyword, String genreId, int page, int size) {
        log.debug("[MockPublicCinemaFacade] Đang dùng mock data — profile: dev");

        MovieResponse mockMovie = MovieResponse.builder()
                .id("mock-movie-id-123")
                .title("Tín Hiệu (Mock Data)")
                .description("Mô tả ảo cho lúc Dev chưa làm xong DB.")
                .director("James Cameron")
                .durationMinutes(150)
                .ageRating("18+")
                .status(MovieStatus.NOW_SHOWING)
                .posterUrl("https://example.com/poster.jpg")
                .build();

        return PaginationResponse.<MovieResponse>builder()
                .currentItems(List.of(mockMovie))
                .totalPages(1)
                .currentPage(page)
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Trả về một lịch chiếu mock cố định thay vì query DB.
     */
    @Override
    public List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date) {
        log.debug("[MockPublicCinemaFacade] Trả về showtime mock cho movieId={}, date={}", movieId, date);

        return List.of(
                ShowtimeResponse.builder()
                        .id("mock-showtime-id")
                        .basePrice(100000)
                        .startTime(date.atTime(18, 0))
                        .endTime(date.atTime(21, 15))
                        .movieId(movieId)
                        .movieTitle("Tín Hiệu (Mock Data)")
                        .auditoriumId("mock-auditorium-id")
                        .auditoriumName("MOCK IMAX 1")
                        .build()
        );
    }
}
