package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.MovieReview;
import com.movie.cinema_booking_backend.response.MovieRatingStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MovieReviewRepository extends JpaRepository<MovieReview, String> {

    boolean existsByUserIdAndMovieId(Long userId, String movieId);

    Optional<MovieReview> findByIdAndUserId(String id, Long userId);

    @Query("SELECT r FROM MovieReview r " +
            "WHERE r.movie.id = :movieId " +
            "AND (:minRating IS NULL OR r.rating >= :minRating) " +
            "AND (:maxRating IS NULL OR r.rating <= :maxRating)")
    Page<MovieReview> findByMovieForAuthenticated(
            @Param("movieId") String movieId,
            @Param("minRating") Double minRating,
            @Param("maxRating") Double maxRating,
            Pageable pageable
    );

    @Query("SELECT r FROM MovieReview r " +
            "WHERE r.user.id = :userId " +
            "AND (:movieId IS NULL OR r.movie.id = :movieId)")
    Page<MovieReview> findMyReviews(
            @Param("userId") Long userId,
            @Param("movieId") String movieId,
            Pageable pageable
    );

    @Query("SELECT r FROM MovieReview r " +
            "WHERE (:movieId IS NULL OR r.movie.id = :movieId) " +
            "AND (:minRating IS NULL OR r.rating >= :minRating) " +
            "AND (:maxRating IS NULL OR r.rating <= :maxRating) " +
            "AND (:fromDate IS NULL OR r.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR r.createdAt <= :toDate) " +
            "AND (:keyword IS NULL OR " +
            "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.movie.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MovieReview> searchForAdmin(
            @Param("movieId") String movieId,
            @Param("minRating") Double minRating,
            @Param("maxRating") Double maxRating,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT new com.movie.cinema_booking_backend.response.MovieRatingStatsResponse(" +
            "m.id, m.title, AVG(r.rating), COUNT(r.id)) " +
            "FROM Movie m LEFT JOIN MovieReview r ON r.movie.id = m.id " +
            "WHERE m.id = :movieId " +
            "GROUP BY m.id, m.title")
    Optional<MovieRatingStatsResponse> getMovieRatingStats(@Param("movieId") String movieId);
}
