package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IGenreService {

    Page<GenreResponse> getAllGenres(int page, int size);

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(String id);

    void createGenre(GenreRequest request);

    GenreResponse updateGenre(String id, GenreRequest request);

    void deleteGenre(String id);
}
