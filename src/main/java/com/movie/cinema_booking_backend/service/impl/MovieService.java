package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.mapper.MovieMapper;
import com.movie.cinema_booking_backend.repository.GenreRepository;
import com.movie.cinema_booking_backend.repository.MovieRepository;
import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.service.IMovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MovieService implements IMovieService {
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMapper movieMapper;

    public MovieService(MovieRepository movieRepository, GenreRepository genreRepository, MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.movieMapper = movieMapper;
    }

    @Override
    @Transactional
    public MovieResponse createMovie(MovieRequest movieRequest) {
        // Check if movie title already exists
        if (movieRepository.existsByTitle(movieRequest.getTitle())) {
            throw new AppException(ErrorCode.MOVIE_EXISTS);
        }

        // Build movie using Builder pattern
        Movie movie = Movie.builder()
                .title(movieRequest.getTitle())
                .description(movieRequest.getDescription())
                .director(movieRequest.getDirector())
                .cast(movieRequest.getCast())
                .durationMinutes(movieRequest.getDurationMinutes())
                .releaseDate(movieRequest.getReleaseDate())
                .posterUrl(movieRequest.getPosterUrl())
                .trailerUrl(movieRequest.getTrailerUrl())
                .ageRating(movieRequest.getAgeRating())
                .status(movieRequest.getStatus())
                .build();

        // Add genres if provided
        assignGenresToMovie(movie, movieRequest.getGenreIds());

        Movie savedMovie = movieRepository.save(movie);
        log.info("Movie created successfully with ID: {}", savedMovie.getId());
        return movieMapper.toResponse(savedMovie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> getAllMovies(int page, int size) {
        log.debug("Fetching all movies. Page: {}, Size: {}", page, size);
        Page<Movie> movies = movieRepository.findAll(PageRequest.of(page, size));
        return movieMapper.toResponsePage(movies);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(String id) {
        log.debug("Fetching movie by ID: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        return movieMapper.toResponse(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieByTitle(String title) {
        log.debug("Fetching movie by title: {}", title);
        Movie movie = movieRepository.findByTitle(title)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        return movieMapper.toResponse(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> getMoviesByStatus(MovieStatus status, int page, int size) {
        log.debug("Fetching movies by status: {}. Page: {}, Size: {}", status, page, size);
        Page<Movie> movies = movieRepository.findByStatus(status, PageRequest.of(page, size));
        return movieMapper.toResponsePage(movies);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> searchMovies(String keyword, int page, int size) {
        log.debug("Searching movies with keyword: '{}'. Page: {}, Size: {}", keyword, page, size);
        Page<Movie> movies = movieRepository.searchByKeyword(keyword, PageRequest.of(page, size));
        return movieMapper.toResponsePage(movies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesByReleaseDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching movies between {} and {}", startDate, endDate);
        return movieMapper.toResponseList(movieRepository.findByReleaseDateRange(startDate, endDate));
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(String id, MovieRequest movieRequest) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        // Check if new title already exists (and it's not the same movie)
        if (!movie.getTitle().equals(movieRequest.getTitle()) &&
                movieRepository.existsByTitle(movieRequest.getTitle())) {
            throw new AppException(ErrorCode.MOVIE_EXISTS);
        }

        // Update movie using toBuilder() pattern - cleaner and safer for partial updates
        movie = movie.toBuilder()
                .title(movieRequest.getTitle())
                .description(movieRequest.getDescription())
                .director(movieRequest.getDirector())
                .cast(movieRequest.getCast())
                .durationMinutes(movieRequest.getDurationMinutes())
                .releaseDate(movieRequest.getReleaseDate())
                .posterUrl(movieRequest.getPosterUrl())
                .trailerUrl(movieRequest.getTrailerUrl())
                .ageRating(movieRequest.getAgeRating())
                .status(movieRequest.getStatus())
                .genres(new ArrayList<>())  // Reset genres with fresh collection
                .build();

        // Add updated genres if provided
        assignGenresToMovie(movie, movieRequest.getGenreIds());

        Movie updatedMovie = movieRepository.save(movie);
        log.info("Movie updated successfully. ID: {}", updatedMovie.getId());
        return movieMapper.toResponse(updatedMovie);
    }

    @Override
    @Transactional
    public void deleteMovie(String id) {
        if (!movieRepository.existsById(id)) {
            log.warn("Movie not found for deletion. ID: {}", id);
            throw new AppException(ErrorCode.MOVIE_NOT_FOUND);
        }
        movieRepository.deleteById(id);
        log.info("Movie deleted successfully. ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteAllMovies() {
        log.warn("Attempting to delete all movies");
        movieRepository.deleteAll();
        log.info("All movies deleted successfully");
    }

    /**
     * Helper method to assign genres to a movie
     * Extracted to eliminate DRY violation between createMovie() and updateMovie()
     * Uses .distinct() to prevent duplicate genre assignments
     */
    private void assignGenresToMovie(Movie movie, List<String> genreIds) {
        if (genreIds != null && !genreIds.isEmpty()) {
            genreIds.stream()
                    .distinct()  // Prevent duplicate genre IDs in the list
                    .forEach(genreId -> {
                        Genre genre = genreRepository.findById(genreId)
                                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTS));
                        movie.addGenre(genre);
                    });
        }
    }
}
