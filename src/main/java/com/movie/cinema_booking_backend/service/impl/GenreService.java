package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.GenreRepository;
import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.service.IGenreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService implements IGenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Override
    public Page<GenreResponse> getAllGenres(int page, int size) {
        var genres = genreRepository.findAll(PageRequest.of(page, size));
        return genres;
    }
    @Override
    public List<GenreResponse> getAllGenres() {
        var genres = genreRepository.findAll();
        return genres.stream()
                .map(genre ->
                        GenreResponse.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .build())
                .toList();
    }

    @Override
    public void createGenre(GenreRequest genreRequest) {
        if(genreRepository.existsByName(genreRequest.getName())) {
            throw new AppException(ErrorCode.GENRE_EXISTS);
        }
        var genre = new Genre();
        genre.setName(genreRequest.getName());
        genreRepository.save(genre);
    }
}
