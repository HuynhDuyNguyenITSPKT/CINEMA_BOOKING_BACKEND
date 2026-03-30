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

/**
 * GenreService - Triển khai business logic cho Genre module.
 *
 * Factory (IGenreFactory) chịu trách nhiệm tạo/map objects.
 * Service chỉ điều phối: validate → factory → repository → return.
 *
 * SOLID — Single Responsibility: Chỉ chứa business logic, không build entity thủ công.
 * SOLID — Dependency Inversion: Phụ thuộc vào IGenreFactory và IGenreService (interfaces).
 */
@Service
@RequiredArgsConstructor
public class GenreService implements IGenreService {

    private final GenreRepository genreRepository;
    private final IGenreFactory genreFactory;

    @Override
    public Page<GenreResponse> getAllGenres(int page, int size) {
        return genreRepository.findAll(PageRequest.of(page, size))
                .map(genreFactory::createGenreResponse);
    }

    @Override
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(genreFactory::createGenreResponse)
                .toList();
    }

    @Override
    public GenreResponse getGenreById(String id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));
        return genreFactory.createGenreResponse(genre);
    }

    @Override
    @Transactional
    public void createGenre(GenreRequest request) {
        if (genreRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new AppException(ErrorCode.GENRE_EXISTS);
        }
        // Factory chịu trách nhiệm build entity — Service chỉ điều phối (SRP)
        Genre genre = genreFactory.createGenreEntity(request);
        genreRepository.save(genre);
    }

    @Override
    @Transactional
    public GenreResponse updateGenre(String id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));

        // Chỉ kiểm tra trùng tên nếu tên thực sự thay đổi
        String newName = request.getName().trim();
        if (!genre.getName().equalsIgnoreCase(newName) && genreRepository.existsByNameIgnoreCase(newName)) {
            throw new AppException(ErrorCode.GENRE_EXISTS);
        }

        // Factory chịu trách nhiệm update fields — không set thủ công từng field ở Service (SRP)
        genreFactory.updateGenreEntity(genre, request);
        Genre saved = genreRepository.save(genre);
        return genreFactory.createGenreResponse(saved);
    }

    @Override
    @Transactional
    public void deleteGenre(String id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));
        genreRepository.delete(genre);
    }
}
