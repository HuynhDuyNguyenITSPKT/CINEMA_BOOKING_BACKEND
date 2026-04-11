package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, String> {

    boolean existsByAuditoriumId(String auditoriumId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM Showtime s WHERE s.auditorium.id = :auditoriumId " +
           "AND s.startTime <= :endTime AND s.endTime >= :startTime")
    boolean existsOverlappingShowtime(@Param("auditoriumId") String auditoriumId, 
                                      @Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId " +
           "AND s.startTime >= :startOfDay AND s.startTime < :endOfDay ORDER BY s.startTime ASC")
    List<Showtime> findShowtimesByMovieAndDate(
            @Param("movieId") String movieId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM Showtime s WHERE s.auditorium.id = :auditoriumId " +
           "AND s.id <> :excludeId " +
           "AND s.startTime <= :endTime AND s.endTime >= :startTime")
    boolean existsOverlappingShowtimeExcluding(@Param("auditoriumId") String auditoriumId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("excludeId") String excludeId);
}
