package com.movie.cinema_booking_backend.service.genre.factory.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.service.genre.factory.IGenreFactory;
import org.springframework.stereotype.Component;

@Component
public class GenreFactory implements IGenreFactory {

    @Override
    public Genre createGenreEntity(GenreRequest request) {
        return Genre.builder()
                .name(request.getName().trim())
                .build();
    }

    @Override
    public void updateGenreEntity(Genre genre, GenreRequest request) {
        genre.setName(request.getName().trim());
    }

    @Override
    public GenreResponse createGenreResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}
