package com.movie.cinema_booking_backend.service.movie.factory.impl;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.request.MovieRequest;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.service.genre.factory.IGenreFactory;
import com.movie.cinema_booking_backend.service.movie.factory.IMovieFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MovieFactory implements IMovieFactory {

    private final IGenreFactory genreFactory;

    @Override
    public Movie createEntity(MovieRequest request, List<Genre> genres) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .director(request.getDirector())
                .cast(request.getCast())
                .durationMinutes(request.getDurationMinutes())
                .releaseDate(request.getReleaseDate())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .ageRating(request.getAgeRating())
                .status(request.getStatus())
                .genres(new ArrayList<>())
                .build();

        genres.forEach(movie::addGenre);
        return movie;
    }

    @Override
    public void updateEntity(Movie movie, MovieRequest request, List<Genre> genres) {
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDirector(request.getDirector());
        movie.setCast(request.getCast());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setAgeRating(request.getAgeRating());
        movie.setStatus(request.getStatus());

        movie.removeAllGenres();
        genres.forEach(movie::addGenre);
    }

    @Override
    public MovieResponse createResponse(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie không thể null");
        }

        List<GenreResponse> genreResponses = movie.getGenres().stream()
                .map(genreFactory::createGenreResponse)
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
}
