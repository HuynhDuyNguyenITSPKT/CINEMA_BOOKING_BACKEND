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
        // Factory nhận trách nhiệm validate - không return null
        if (request == null) {
            throw new IllegalArgumentException("GenreRequest không thể null");
        }
        return Genre.builder()
                .name(request.getName())
                .build();
    }

    @Override
    public void updateGenreEntity(Genre genre, GenreRequest request) {
        // Factory nhận trách nhiệm validate
        if (genre == null) {
            throw new IllegalArgumentException("Genre không thể null");
        }
        if (request == null) {
            throw new IllegalArgumentException("GenreRequest không thể null");
        }
        genre.setName(request.getName());
    }

    @Override
    public GenreResponse createGenreResponse(Genre genre) {
        // Factory nhận trách nhiệm validate - không return null
        if (genre == null) {
            throw new IllegalArgumentException("Genre không thể null");
        }
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}
