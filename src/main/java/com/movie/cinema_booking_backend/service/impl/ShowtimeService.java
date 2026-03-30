package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.entity.Showtime;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AuditoriumRepository;
import com.movie.cinema_booking_backend.repository.MovieRepository;
import com.movie.cinema_booking_backend.repository.ShowtimeRepository;
import com.movie.cinema_booking_backend.request.ShowtimeRequest;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.IShowtimeService;
import com.movie.cinema_booking_backend.service.showtime.factory.IShowtimeFactory;
import com.movie.cinema_booking_backend.service.showtime.strategy.PricingStrategyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ShowtimeService - Triển khai business logic cho Showtime (lịch chiếu).
 *
 * SOLID — Single Responsibility: Service điều phối, Factory build, Strategy tính giá.
 * SOLID — Open/Closed: Thêm chiến lược giá mới không cần sửa service.
 * SOLID — Dependency Inversion: Phụ thuộc vào PricingStrategyContext, IShowtimeFactory.
 *
 * Design Patterns:
 * - Strategy (PricingStrategyContext): tính giá theo giờ cao điểm/thấp điểm
 * - Factory (IShowtimeFactory): tạo/map Showtime entity và DTO
 */
@Service
@RequiredArgsConstructor
public class ShowtimeService implements IShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final PricingStrategyContext pricingStrategyContext;
    private final IShowtimeFactory showtimeFactory;

    @Override
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        Auditorium auditorium = auditoriumRepository.findById(request.getAuditoriumId())
                .orElseThrow(() -> new AppException(ErrorCode.AUDITORIUM_NOT_FOUND));

        // Bàn giao việc tính endTime (kèm 15 phút dọn dẹp) cho chính đối tượng Movie tự quản lý (OOP: Tell, Don't Ask)
        LocalDateTime endTime = movie.calculateEndTimeWithCleaning(request.getStartTime());

        // Check đụng độ lịch chiếu
        boolean isOverlapping = showtimeRepository.existsOverlappingShowtime(
                auditorium.getId(), request.getStartTime(), endTime);

        if (isOverlapping) {
            throw new AppException(ErrorCode.OVERLAPPING_SHOWTIME);
        }

        // Tính giá vé sử dụng Strategy Context dựa trên basePrice admin truyền vào
        int basePrice = pricingStrategyContext.getPrice(request.getStandardPrice(), request.getStartTime());

        Showtime showtime = showtimeFactory.createEntity(movie, auditorium, request.getStartTime(), endTime, basePrice);
        showtime = showtimeRepository.save(showtime);

        return showtimeFactory.createResponse(showtime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Showtime> showtimes = showtimeRepository.findShowtimesByMovieAndDate(movieId, startOfDay, endOfDay);

        return showtimes.stream()
                .map(showtimeFactory::createResponse)
                .collect(Collectors.toList());
    }
}
