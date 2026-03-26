package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.ApiResponseBuilder;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IMovieService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/movies")
@Validated
@Slf4j
public class MovieController {

    private final IMovieService movieService;

    public MovieController(IMovieService movieService) {
        this.movieService = movieService;
    }

    // CREATE - POST /api/movies
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<?> createMovie(@Valid @RequestBody MovieRequest movieRequest) {
        log.info("Creating new movie: {}", movieRequest.getTitle());
        var movieResponse = movieService.createMovie(movieRequest);
        return ApiResponseBuilder.success(movieResponse, "Create movie successfully");
    }

    // READ - GET /api/movies (pageable, with optional page/size params)
    @GetMapping("")
    public ApiResponse<?> getAllMovies(@RequestParam(defaultValue = "0") @Min(0) int page,
                                       @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        log.debug("Fetching all movies. Page: {}, Size: {}", page, size);
        var pageResult = movieService.getAllMovies(page, size);
        return ApiResponseBuilder.success(buildPaginationResponse(pageResult), "Get all movies successfully");
    }

    // READ - GET /api/movies/{id}
    @GetMapping("/{id}")
    public ApiResponse<?> getMovieById(@PathVariable String id) {
        log.debug("Fetching movie by ID: {}", id);
        var movie = movieService.getMovieById(id);
        return ApiResponseBuilder.success(movie, "Get movie successfully");
    }

    // READ - GET /api/movies/title/{title}
    @GetMapping("/title/{title}")
    public ApiResponse<?> getMovieByTitle(@PathVariable String title) {
        log.debug("Fetching movie by title: {}", title);
        var movie = movieService.getMovieByTitle(title);
        return ApiResponseBuilder.success(movie, "Get movie by title successfully");
    }

    // READ - GET /api/movies/status/{status}
    @GetMapping("/status/{status}")
    public ApiResponse<?> getMoviesByStatus(@PathVariable MovieStatus status,
                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        log.debug("Fetching movies by status: {}. Page: {}, Size: {}", status, page, size);
        var pageResult = movieService.getMoviesByStatus(status, page, size);
        return ApiResponseBuilder.success(buildPaginationResponse(pageResult), "Get movies by status successfully");
    }

    // READ - GET /api/movies/search
    @GetMapping("/search")
    public ApiResponse<?> searchMovies(@RequestParam @jakarta.validation.constraints.NotBlank String keyword,
                                       @RequestParam(defaultValue = "0") @Min(0) int page,
                                       @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        log.debug("Searching movies with keyword: '{}'. Page: {}, Size: {}", keyword, page, size);
        var pageResult = movieService.searchMovies(keyword, page, size);
        return ApiResponseBuilder.success(buildPaginationResponse(pageResult), "Search movies successfully");
    }

    // READ - GET /api/movies/by-date-range
    @GetMapping("/by-date-range")
    public ApiResponse<?> getMoviesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("Fetching movies between {} and {}", startDate, endDate);
        var movies = movieService.getMoviesByReleaseDateRange(startDate, endDate);
        return ApiResponseBuilder.success(movies, "Get movies by release date range successfully");
    }

    // UPDATE - PUT /api/movies/{id}
    @PutMapping("/{id}")
    public ApiResponse<?> updateMovie(@PathVariable String id,
                                      @Valid @RequestBody MovieRequest movieRequest) {
        log.info("Updating movie. ID: {}", id);
        var movieResponse = movieService.updateMovie(id, movieRequest);
        return ApiResponseBuilder.success(movieResponse, "Update movie successfully");
    }

    // DELETE - DELETE /api/movies/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteMovie(@PathVariable String id) {
        log.info("Deleting movie. ID: {}", id);
        movieService.deleteMovie(id);
        return ApiResponseBuilder.success("Delete movie successfully");
    }

    // DELETE - DELETE /api/movies
    @DeleteMapping("")
    public ApiResponse<?> deleteAllMovies() {
        log.warn("Attempting to delete all movies");
        movieService.deleteAllMovies();
        return ApiResponseBuilder.success("Delete all movies successfully");
    }

    /**
     * Helper method to build pagination response
     * Extracted to eliminate DRY violation across 3 endpoints
     */
    private PaginationResponse<MovieResponse> buildPaginationResponse(org.springframework.data.domain.Page<MovieResponse> pageResult) {
        return new PaginationResponse.Builder<MovieResponse>()
                .currentItems(pageResult.getContent())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .build();
    }
}
