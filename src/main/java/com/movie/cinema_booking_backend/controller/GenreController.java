package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IGenreService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/genres")
@Validated
public class GenreController {

    private final IGenreService genreService;

    public GenreController(IGenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping("/pageable")
    public ApiResponse<?> getAllGenresPageable(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        var pageResult = genreService.getAllGenres(page, size);

        var pagination = new PaginationResponse.Builder<GenreResponse>()
                .currentItems(pageResult.getContent())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .build();

        return new ApiResponse.Builder<>()
                .success(true)
                .message("Get all genres successfully")
                .data(
                       pagination
                )
                .build();
    }
    @GetMapping("")
    public ApiResponse<?> getAllGenres() {
        var genres = genreService.getAllGenres();
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Get all genres successfully")
                .data(genres)
                .build();
    }

    @PostMapping("")
    public ApiResponse<?> createGenre(@Valid @RequestBody GenreRequest genreRequest) {
        genreService.createGenre(genreRequest);
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Create genre successfully")
                .build();
    }

}
