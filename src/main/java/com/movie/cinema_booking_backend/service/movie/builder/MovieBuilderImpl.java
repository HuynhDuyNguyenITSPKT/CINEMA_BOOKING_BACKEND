package com.movie.cinema_booking_backend.service.movie.builder;

import com.movie.cinema_booking_backend.entity.Genre;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.request.MovieRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MovieBuilderImpl implements IMovieBuilder {

    private MovieRequest request;
    private List<Genre> genres = new ArrayList<>();

    @Override
    public IMovieBuilder withBasicInfo(MovieRequest request) {
        this.request = request;
        return this;
    }

    @Override
    public IMovieBuilder withGenres(List<Genre> genres) {
        this.genres = genres != null ? genres : new ArrayList<>();
        return this;
    }

    @Override
    public Movie build() {
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
}
