package com.movie.cinema_booking_backend.mapper;

import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.response.MovieResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovieMapper {

    /**
     * Convert Movie entity to MovieResponse DTO
     * Follows Single Responsibility Principle
     */
    public MovieResponse toResponse(Movie movie) {
        if (movie == null) {
            return null;
        }

        List<GenreResponse> genreResponses = movie.getGenres().stream()
                .map(genre -> GenreResponse.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .build())
                .toList();

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .durationMinutes(movie.getDurationMinutes())
                .releaseDate(movie.getReleaseDate())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .ageRating(movie.getAgeRating())
                .status(movie.getStatus())
                .genres(genreResponses)
                .build();
    }

    /**
     * Convert Page of Movies to Page of MovieResponses
     * Reduces code duplication in service layer
     */
    public Page<MovieResponse> toResponsePage(Page<Movie> movies) {
        if (movies == null) {
            return Page.empty();
        }
        return movies.map(this::toResponse);
    }

    /**
     * Convert List of Movies to List of MovieResponses
     * Reduces code duplication in service layer
     */
    public List<MovieResponse> toResponseList(List<Movie> movies) {
        if (movies == null) {
            return List.of();
        }
        return movies.stream()
                .map(this::toResponse)
                .toList();
    }
}
