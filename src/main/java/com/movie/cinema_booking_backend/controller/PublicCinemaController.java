package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.movie.facade.IPublicCinemaFacade;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/public/cinema")
@RequiredArgsConstructor
public class PublicCinemaController {

    private final IPublicCinemaFacade publicCinemaFacade;

    @GetMapping("/movies")
    public ApiResponse<PaginationResponse<MovieResponse>> searchMovies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genreId,
            @RequestParam(defaultValue = "NOW_SHOWING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        MovieStatus movieStatus = resolvePublicMovieStatus(status);
        PaginationResponse<MovieResponse> response = publicCinemaFacade.searchAndFilterMovies(
                keyword, genreId, movieStatus, page, size);

        return ApiResponse.<PaginationResponse<MovieResponse>>builder()
                .success(true)
                .message("Lấy danh sách phim thành công")
                .data(response)
                .build();
    }

    private MovieStatus resolvePublicMovieStatus(String status) {
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        return switch (normalizedStatus) {
            case "NOW_SHOWING" -> MovieStatus.NOW_SHOWING;
            case "COMING_SOON" -> MovieStatus.COMING_SOON;
            default -> throw new AppException(ErrorCode.INVALID_MOVIE_STATUS);
        };
    }

    @GetMapping("/movies/{movieId}")
    public ApiResponse<MovieResponse> getMovieById(@PathVariable String movieId) {
        return ApiResponse.<MovieResponse>builder()
                .success(true)
                .message("Lấy thông tin phim thành công")
                .data(publicCinemaFacade.getMovieById(movieId))
                .build();
    }

    @GetMapping("/movies/{movieId}/showtimes")
    public ApiResponse<List<ShowtimeResponse>> getShowtimes(
            @PathVariable String movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<ShowtimeResponse> response = publicCinemaFacade.getShowtimesByMovieAndDate(movieId, date);

        return ApiResponse.<List<ShowtimeResponse>>builder()
                .success(true)
                .message("Lấy danh sách lịch chiếu thành công")
                .data(response)
                .build();
    }
}
