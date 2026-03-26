package com.movie.cinema_booking_backend.facade.impl;

import com.movie.cinema_booking_backend.facade.MovieFacade;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.service.IMovieService;
import com.movie.cinema_booking_backend.enums.MovieStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.List;

/**
 * THỰC PRODUCTION Implementation của MovieFacade
 * 
 * Dùng khi @Profile("prod") - trong production
 * Gọi tới MovieService THỰC để lấy dữ liệu từ database
 * 
 * Advantage: 
 * 1. BookingService (hoặc service khác) inject MovieFacade thôi
 * 2. Không cần thay code → chỉ thay spring.profiles.active
 * 3. Dev team có thể xài mock, Prod dùng real
 */
@Component
@Profile("prod")  // ← Chỉ active khi spring.profiles.active=prod (default)
@Slf4j
public class MovieFacadeImpl implements MovieFacade {
    
    private final IMovieService movieService;
    
    public MovieFacadeImpl(IMovieService movieService) {
        this.movieService = movieService;
    }
    
    @Override
    public MovieResponse getMovieById(String id) {
        log.info("[PROD] Getting movie by ID: {}", id);
        return movieService.getMovieById(id);
    }
    
    @Override
    public MovieResponse getMovieByTitle(String title) {
        log.info("[PROD] Getting movie by title: {}", title);
        return movieService.getMovieByTitle(title);
    }
    
    @Override
    public Page<MovieResponse> getAllMovies(int page, int size) {
        log.info("[PROD] Getting all movies. Page: {}, Size: {}", page, size);
        return movieService.getAllMovies(page, size);
    }
    
    @Override
    public Page<MovieResponse> getMoviesByStatus(String status, int page, int size) {
        log.info("[PROD] Getting movies by status: {}", status);
        try {
            MovieStatus movieStatus = MovieStatus.valueOf(status.toUpperCase());
            return movieService.getMoviesByStatus(movieStatus, page, size);
        } catch (IllegalArgumentException e) {
            log.warn("[PROD] Invalid movie status: {}", status);
            throw new RuntimeException("Invalid movie status: " + status);
        }
    }
    
    @Override
    public Page<MovieResponse> searchMovies(String keyword, int page, int size) {
        log.info("[PROD] Searching movies with keyword: {}", keyword);
        return movieService.searchMovies(keyword, page, size);
    }
    
    @Override
    public List<MovieResponse> getMoviesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("[PROD] Getting movies between {} and {}", startDate, endDate);
        return movieService.getMoviesByReleaseDateRange(startDate, endDate);
    }
}
