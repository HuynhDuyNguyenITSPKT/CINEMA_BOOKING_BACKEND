package com.movie.cinema_booking_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String director;

    @Column(length = 500)
    private String cast;

    private int durationMinutes;

    private LocalDate releaseDate;

    @Column(length = 500)
    private String posterUrl;

    @Column(length = 500)
    private String trailerUrl;

    private int ageRestriction;

    @Column(length = 255)
    private String status;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres = new ArrayList<>();
}
