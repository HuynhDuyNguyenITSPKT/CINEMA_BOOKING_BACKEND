package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.facade.MovieFacade;
import com.movie.cinema_booking_backend.response.MovieResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * EXAMPLE: Consumer Service sử dụng MovieFacade
 * 
 * KỲ DIỆU:
 * 1. BookingService inject MovieFacade (INTERFACE) → KHÔNG BIẾT IMPL NÀO
 * 2. Spring tự động chọn:
 *    - Nếu @Profile("dev") active → dùng MockMovieFacade
 *    - Nếu @Profile("prod") active → dùng MovieFacadeImpl
 * 3. CODE KHÔNG THAY ĐỔI! Chỉ cần thay spring.profiles.active
 * 
 * LỢI ÍCH:
 * ✅ Dev team viết mock data → không chặn prod team
 * ✅ Prod team viết real logic → không cần chờ mock
 * ✅ Testing dễ → chỉ thay profile
 * ✅ Không phụ thuộc interface bét
 */
@Service
@Slf4j
@Transactional
public class BookingService {
    
    /**
     * ⭐ QUAN TRỌNG: Inject INTERFACE (MovieFacade) KHÔNG PHẢI IMPL
     * 
     * Spring sẽ tự động:
     * - Dev mode: chọn MockMovieFacade (@Profile("dev"))
     * - Prod mode: chọn MovieFacadeImpl (@Profile("prod"))
     * 
     * BookingService không quan tâm!
     */
    private final MovieFacade movieFacade;
    
    public BookingService(MovieFacade movieFacade) {
        this.movieFacade = movieFacade;
    }
    
    /**
     * ❌ TRƯỚC (lỗi):
     * private final MovieService movieService;  // ← Phụ thuộc implementation cụ thể
     * 
     * ✅ GIỜ (đúng):
     * private final MovieFacade movieFacade;    // ← Phụ thuộc interface chung
     */
    
    // ==========================================
    // Business Methods
    // ==========================================
    
    public void bookMovie(String movieId, String userId) {
        log.info("Booking movie: {} for user: {}", movieId, userId);
        
        // 🎬 Lấy movie từ facade
        // Không cần biết từ Mock hay Real
        MovieResponse movie = movieFacade.getMovieById(movieId);
        
        if (movie == null) {
            throw new RuntimeException("Movie not found");
        }
        
        log.info("Booking movie: {} ({})", movie.getTitle(), movie.getAgeRating());
        // ... logic booking tiếp tục
    }
    
    public void bookMovieByTitle(String title, String userId) {
        log.info("Booking movie by title: {} for user: {}", title, userId);
        
        MovieResponse movie = movieFacade.getMovieByTitle(title);
        if (movie == null) {
            throw new RuntimeException("Movie not found");
        }
        
        log.info("Booking movie: {} with duration: {} minutes", title, movie.getDurationMinutes());
        // ... logic booking tiếp tục
    }
    
    public Page<MovieResponse> getAvailableMovies(int page, int size) {
        log.info("Fetching available movies for booking. Page: {}, Size: {}", page, size);
        
        // Facade sẽ trả mock data (dev) hoặc real data (prod)
        // Code KHÔNG THAY ĐỔI!
        return movieFacade.getAllMovies(page, size);
    }
    
    public List<MovieResponse> getRecentMovies(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching movies released between {} and {}", startDate, endDate);
        
        // Lấy từ facade - bất kể mock hay real
        return movieFacade.getMoviesByDateRange(startDate, endDate);
    }
    
    public Page<MovieResponse> searchAvailableMovies(String keyword, int page, int size) {
        log.info("Searching movies with keyword: {}", keyword);
        
        return movieFacade.searchMovies(keyword, page, size);
    }
    
    /**
     * SWITCHING GIỮA DEV/PROD:
     * 
     * application.yaml:
     * 
     * 🔵 DEV MODE:
     *     spring:
     *       profiles:
     *         active: dev
     *     
     *     → MockMovieFacade active
     *     → Trả hardcoded test data
     *     → Dev team xài ngay, không chặn prod team
     * 
     * 🔴 PROD MODE:
     *     spring:
     *       profiles:
     *         active: prod
     *     
     *     → MovieFacadeImpl active
     *     → Gọi MovieService thực
     *     → Lấy dữ liệu từ database
     * 
     * CODE ĐỀN KHÔNG D THAY ĐỔI! 🎉
     */
}
