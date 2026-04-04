package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.GenreRepository;
import com.movie.cinema_booking_backend.request.GenreRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.service.IGenreService;
import com.movie.cinema_booking_backend.service.genre.factory.IGenreFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService implements IGenreService {
    private final GenreRepository genreRepository;
    private final IGenreFactory genreFactory;

    @Override
    public Page<GenreResponse> getAllGenres(int page, int size) {
        Page<Genre> genres = genreRepository.findAll(PageRequest.of(page, size));
        return genres.map(genreFactory::createGenreResponse);
    }

    @Override
    public List<GenreResponse> getAllGenres() {
        List<Genre> genres = genreRepository.findAll();
        return genres.stream()
                .map(genreFactory::createGenreResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GenreResponse getGenreById(String id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));
        return genreFactory.createGenreResponse(genre);
    }

    @Override
    @Transactional
    public void createGenre(GenreRequest genreRequest) {
        if(genreRepository.existsByNameIgnoreCase(genreRequest.getName().trim())) {
            throw new AppException(ErrorCode.GENRE_EXISTS);
        }
        Genre genre = genreFactory.createGenreEntity(genreRequest);
        genreRepository.save(genre);
    }

    @Override
    @Transactional
    public GenreResponse updateGenre(String id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));

        String newName = request.getName().trim();
        if (!genre.getName().equalsIgnoreCase(newName)
                && genreRepository.existsByNameIgnoreCase(newName)) {
            throw new AppException(ErrorCode.GENRE_EXISTS);
        }

        genreFactory.updateGenreEntity(genre, request);
        genre = genreRepository.save(genre);

        return genreFactory.createGenreResponse(genre);
    }

    @Override
    @Transactional
    public void deleteGenre(String id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));
        genreRepository.delete(genre);
    }
}
