package com.movie.cinema_booking_backend.service.movie.factory;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;

public interface IGenreFactory {

    Genre createGenreEntity(GenreRequest request);

    void updateGenreEntity(Genre genre, GenreRequest request);

    GenreResponse createGenreResponse(Genre genre);
}
