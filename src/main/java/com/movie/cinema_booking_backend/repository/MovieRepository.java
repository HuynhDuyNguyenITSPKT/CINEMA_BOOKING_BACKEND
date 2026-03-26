package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {
    boolean existsByTitle(String title);

    Optional<Movie> findByTitle(String title);

    Page<Movie> findAll(Pageable pageable);

    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Movie> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate BETWEEN :startDate AND :endDate")
    List<Movie> findByReleaseDateRange(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}
