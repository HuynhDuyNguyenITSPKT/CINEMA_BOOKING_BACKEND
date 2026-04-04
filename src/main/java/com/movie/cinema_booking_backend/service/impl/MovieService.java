package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.GenreRepository;
import com.movie.cinema_booking_backend.repository.MovieRepository;
import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IMovieService;
import com.movie.cinema_booking_backend.service.movie.builder.IMovieBuilder;
import com.movie.cinema_booking_backend.service.movie.factory.IMovieFactory;
import com.movie.cinema_booking_backend.service.movie.observer.MovieEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService implements IMovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final IMovieBuilder movieBuilder;
    private final IMovieFactory movieFactory;
    private final MovieEventPublisher movieEventPublisher;

    @Override
    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        if (movieRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.MOVIE_TITLE_EXISTS);
        }

        List<Genre> genres = resolveGenres(request.getGenreIds());
        Movie movie = movieBuilder.withBasicInfo(request).withGenres(genres).build();
        movie = movieRepository.save(movie);

        MovieResponse response = movieFactory.createResponse(movie);
        movieEventPublisher.notifyMovieAdded(response);
        return response;
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(String id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        if (!movie.getTitle().equals(request.getTitle())
                && movieRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.MOVIE_TITLE_EXISTS);
        }

        List<Genre> genres = resolveGenres(request.getGenreIds());
        movieFactory.updateEntity(movie, request, genres);
        movie = movieRepository.save(movie);

        MovieResponse response = movieFactory.createResponse(movie);
        movieEventPublisher.notifyMovieUpdated(response);
        return response;
    }

    @Override
    @Transactional
    public void deleteMovie(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        movie.removeAllGenres();
        movieRepository.delete(movie);

        movieEventPublisher.notifyMovieDeleted(id);
    }

    @Override
    public MovieResponse getMovieById(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        return movieFactory.createResponse(movie);
    }

    @Override
    public Page<MovieResponse> getAllMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAll(pageable).map(movieFactory::createResponse);
    }

    @Override
    public PaginationResponse<MovieResponse> searchNowShowingMovies(
            String keyword, String genreId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviePage = movieRepository.searchAndFilterShowingMovies(
                keyword, genreId, MovieStatus.NOW_SHOWING, pageable);

        List<MovieResponse> items = moviePage.getContent().stream()
                .map(movieFactory::createResponse)
                .toList();

        return PaginationResponse.<MovieResponse>builder()
                .currentItems(items)
                .totalItems(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .currentPage(moviePage.getNumber())
                .build();
    }

    private List<Genre> resolveGenres(List<String> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size()) {
            throw new AppException(ErrorCode.GENRE_NOT_FOUND);
        }
        return genres;
    }
}
