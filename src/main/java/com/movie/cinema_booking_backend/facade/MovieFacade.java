package com.movie.cinema_booking_backend.facade;

import com.movie.cinema_booking_backend.response.MovieResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

/**
 * Facade Interface cho Movie Service
 * 
 * Contract để lấy dữ liệu Movie từ Movie Service
 * Client (như BookingService) không cần biết:
 * - Gọi API hay database
 * - Mock hay real implementation
 * 
 * Lợi ích:
 * - Decoupling: BookingService không phụ thuộc MovieService trực tiếp
 * - Parallelizable: Team có thể dev riêng rồi swap implementation
 * - Testable: Dễ mock trong unit test
 */
public interface MovieFacade {
    
    /**
     * Lấy movie theo ID
     */
    MovieResponse getMovieById(String id);
    
    /**
     * Lấy movie theo title
     */
    MovieResponse getMovieByTitle(String title);
    
    /**
     * Lấy danh sách movie (pageable)
     */
    Page<MovieResponse> getAllMovies(int page, int size);
    
    /**
     * Lấy movie theo status
     */
    Page<MovieResponse> getMoviesByStatus(String status, int page, int size);
    
    /**
     * Tìm movie theo keyword
     */
    Page<MovieResponse> searchMovies(String keyword, int page, int size);
    
    /**
     * Lấy movie theo date range
     */
    List<MovieResponse> getMoviesByDateRange(LocalDate startDate, LocalDate endDate);
}
