package com.movie.cinema_booking_backend.entity;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Movie - JPA Entity đại diện cho một bộ phim trong hệ thống.
 *
 * OOP: Tự quản lý logic tính thời gian (Tell, Don't Ask).
 * Bidirectional relationship với Genre được quản lý qua addGenre() và removeAllGenres().
 */
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

    /**
     * Thêm genre vào phim và duy trì bidirectional relationship.
     * OOP: Entity tự quản lý quan hệ của mình.
     *
     * @param genre genre cần liên kết
     */
    public void addGenre(Genre genre) {
        this.genres.add(genre);
        genre.getMovies().add(this);
    }

    /**
     * Xóa tất cả genres khỏi phim, đồng thời xóa back-reference trên Genre.
     * Gọi trước khi set danh sách genre mới khi update.
     * OOP: Tránh orphan references trong bidirectional relationship.
     */
    public void removeAllGenres() {
        for (Genre genre : this.genres) {
            genre.getMovies().remove(this);
        }
        this.genres.clear();
    }

    // ============================================================
    // Business Logic — OOP: Tell, Don't Ask
    // ============================================================

    private static final int CLEANING_BUFFER_MINUTES = 15;

    /**
     * Tính thời gian kết thúc buổi chiếu bao gồm 15 phút dọn dẹp phòng.
     * Entity tự quản lý logic liên quan đến durationMinutes của chính nó.
     *
     * @param startTime thời gian bắt đầu buổi chiếu
     * @return thời gian kết thúc = startTime + durationMinutes + 15 phút buffer
     */
    public LocalDateTime calculateEndTimeWithCleaning(LocalDateTime startTime) {
        return startTime.plusMinutes(this.durationMinutes).plusMinutes(CLEANING_BUFFER_MINUTES);
    }
}
