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

@Slf4j
@Component
@Profile("dev")
public class MockPublicCinemaFacade implements IPublicCinemaFacade {

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
                .totalItems(1)
                .totalPages(1)
                .currentPage(page)
                .build();
    }

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
