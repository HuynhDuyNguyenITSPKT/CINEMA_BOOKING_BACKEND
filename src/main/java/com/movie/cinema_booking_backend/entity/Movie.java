package com.movie.cinema_booking_backend.entity;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(nullable = false)
    private String ageRating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Genre> genres = new ArrayList<>();

    public void addGenre(Genre genre) {
        this.genres.add(genre);
        genre.getMovies().add(this);
    }

    public void removeAllGenres() {
        for (Genre genre : this.genres) {
            genre.getMovies().remove(this);
        }
        this.genres.clear();
    }

    private static final int CLEANING_BUFFER_MINUTES = 15;

    public LocalDateTime calculateEndTimeWithCleaning(LocalDateTime startTime) {
        return startTime.plusMinutes(this.durationMinutes).plusMinutes(CLEANING_BUFFER_MINUTES);
    }
}
