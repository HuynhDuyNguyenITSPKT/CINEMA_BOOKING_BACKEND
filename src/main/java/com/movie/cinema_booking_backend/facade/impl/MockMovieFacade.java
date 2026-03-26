package com.movie.cinema_booking_backend.facade.impl;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import com.movie.cinema_booking_backend.facade.MovieFacade;
import com.movie.cinema_booking_backend.response.GenreResponse;
import com.movie.cinema_booking_backend.response.MovieResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MOCK Implementation của MovieFacade
 * 
 * Dùng khi @Profile("dev") - trong quá trình development
 * Trả về dummy data để các service khác có thể dev
 * KHÔNG cần gọi Movie Service (thật)
 * 
 * Khi Movie Service dev xong → swap sang MovieFacadeImpl (prod)
 * BookingService/khác code không cần thay đổi!
 */
@Component
@Profile("dev")  // ← Chỉ active khi spring.profiles.active=dev
@Slf4j
public class MockMovieFacade implements MovieFacade {
    
    @Override
    public MovieResponse getMovieById(String id) {
        log.info("[MOCK] Getting movie by ID: {}", id);
        return createMockMovie(id, "Inception");
    }
    
    @Override
    public MovieResponse getMovieByTitle(String title) {
        log.info("[MOCK] Getting movie by title: {}", title);
        return createMockMovie("movie-1", title);
    }
    
    @Override
    public Page<MovieResponse> getAllMovies(int page, int size) {
        log.info("[MOCK] Getting all movies. Page: {}, Size: {}", page, size);
        
        List<MovieResponse> mockMovies = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            mockMovies.add(createMockMovie("movie-" + i, "Movie " + i));
        }
        
        return new PageImpl<>(mockMovies, 
            org.springframework.data.domain.PageRequest.of(page, size), 
            100);  // Total 100 movies
    }
    
    @Override
    public Page<MovieResponse> getMoviesByStatus(String status, int page, int size) {
        log.info("[MOCK] Getting movies by status: {}", status);
        
        List<MovieResponse> mockMovies = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            mockMovies.add(createMockMovie("movie-" + i, "Active Movie " + i));
        }
        
        return new PageImpl<>(mockMovies, 
            org.springframework.data.domain.PageRequest.of(page, size), 
            50);
    }
    
    @Override
    public Page<MovieResponse> searchMovies(String keyword, int page, int size) {
        log.info("[MOCK] Searching movies with keyword: {}", keyword);
        
        List<MovieResponse> mockMovies = new ArrayList<>();
        mockMovies.add(createMockMovie("movie-1", keyword + " Movie 1"));
        mockMovies.add(createMockMovie("movie-2", keyword + " Movie 2"));
        
        return new PageImpl<>(mockMovies, 
            org.springframework.data.domain.PageRequest.of(page, size), 
            2);
    }
    
    @Override
    public List<MovieResponse> getMoviesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("[MOCK] Getting movies between {} and {}", startDate, endDate);
        
        List<MovieResponse> mockMovies = new ArrayList<>();
        mockMovies.add(createMockMovie("movie-1", "Movie Released in Range 1"));
        mockMovies.add(createMockMovie("movie-2", "Movie Released in Range 2"));
        
        return mockMovies;
    }
    
    // ==========================================
    // Helper method - tạo mock movie objects
    // ==========================================
    
    private MovieResponse createMockMovie(String id, String title) {
        List<GenreResponse> genres = List.of(
            GenreResponse.builder().id("genre-1").name("Sci-Fi").build(),
            GenreResponse.builder().id("genre-2").name("Thriller").build()
        );
        
        return MovieResponse.builder()
                .id(id)
                .title(title)
                .description("[MOCK] " + title + " description")
                .director("[MOCK] Director Name")
                .cast("[MOCK] Actor 1, Actor 2")
                .durationMinutes(148)
                .releaseDate(LocalDate.now().minusMonths(1))
                .posterUrl("[MOCK] https://example.com/poster.jpg")
                .trailerUrl("[MOCK] https://example.com/trailer.mp4")
                .ageRating("PG-13")
                .status(MovieStatus.NOW_SHOWING)
                .genres(genres)
                .build();
    }
}
