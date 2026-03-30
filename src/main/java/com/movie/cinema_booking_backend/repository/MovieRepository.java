package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.movie.cinema_booking_backend.enums.MovieStatus;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {
    boolean existsByTitle(String title);

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN m.genres g " +
           "WHERE (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:genreId IS NULL OR g.id = :genreId) " +
           "AND m.status = :status")
    Page<Movie> searchAndFilterShowingMovies(@Param("keyword") String keyword, 
                                             @Param("genreId") String genreId, 
                                             @Param("status") MovieStatus status, 
                                             Pageable pageable);
}
