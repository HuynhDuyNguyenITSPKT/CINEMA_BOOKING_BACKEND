package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.entity.MovieReview;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.MovieRepository;
import com.movie.cinema_booking_backend.repository.MovieReviewRepository;
import com.movie.cinema_booking_backend.repository.TicketRepository;
import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.request.AdminMovieReviewRequest;
import com.movie.cinema_booking_backend.request.MovieReviewRequest;
import com.movie.cinema_booking_backend.request.MovieReviewUpdateRequest;
import com.movie.cinema_booking_backend.response.MovieRatingStatsResponse;
import com.movie.cinema_booking_backend.response.MovieReviewResponse;
import com.movie.cinema_booking_backend.service.IMovieReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieReviewService implements IMovieReviewService {

    private static final Set<TicketStatus> ALLOWED_REVIEW_TICKET_STATUSES =
            EnumSet.of(TicketStatus.BOOKED, TicketStatus.USED);

    private final MovieReviewRepository movieReviewRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public MovieReviewResponse createReview(String username, MovieReviewRequest request) {
        User currentUser = getCurrentUser(username);
        Movie movie = getMovieOrThrow(request.getMovieId());

        if (movieReviewRepository.existsByUserIdAndMovieId(currentUser.getId(), movie.getId())) {
            throw new AppException(ErrorCode.MOVIE_REVIEW_ALREADY_EXISTS);
        }

        MovieReview review = MovieReview.builder()
                .movie(movie)
                .user(currentUser)
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .createdAt(LocalDate.now())
                .build();

        return mapToResponse(movieReviewRepository.save(review));
    }

    @Override
    @Transactional
    public MovieReviewResponse updateMyReview(String reviewId, String username, MovieReviewUpdateRequest request) {
        User currentUser = getCurrentUser(username);

        MovieReview review = movieReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.MOVIE_REVIEW_FORBIDDEN);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());

        return mapToResponse(movieReviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteMyReview(String reviewId, String username) {
        User currentUser = getCurrentUser(username);

        MovieReview review = movieReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.MOVIE_REVIEW_FORBIDDEN);
        }

        movieReviewRepository.delete(review);
    }

    @Override
    public Page<MovieReviewResponse> getMovieReviewsForAuthenticated(
            String movieId,
            Double minRating,
            Double maxRating,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        validateRatingRange(minRating, maxRating);
        getMovieOrThrow(movieId);

        PageRequest pageRequest = buildPageRequest(page, size, sortBy, sortDir);

        return movieReviewRepository.findByMovieForAuthenticated(movieId, minRating, maxRating, pageRequest)
                .map(this::mapToResponse);
    }

    @Override
    public Page<MovieReviewResponse> getMyReviews(
            String username,
            String movieId,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        User currentUser = getCurrentUser(username);
        if (movieId != null && !movieId.trim().isEmpty()) {
            getMovieOrThrow(movieId.trim());
        }

        PageRequest pageRequest = buildPageRequest(page, size, sortBy, sortDir);

        return movieReviewRepository.findMyReviews(
                        currentUser.getId(),
                        normalizeToNull(movieId),
                        pageRequest
                )
                .map(this::mapToResponse);
    }

    @Override
    public MovieRatingStatsResponse getMovieRatingStats(String movieId) {
        getMovieOrThrow(movieId);
        return movieReviewRepository.getMovieRatingStats(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
    }

    @Override
    @Transactional
    public MovieReviewResponse createReviewByAdmin(AdminMovieReviewRequest request) {
        User user = getUserOrThrow(request.getUserId());
        Movie movie = getMovieOrThrow(request.getMovieId());

        if (movieReviewRepository.existsByUserIdAndMovieId(user.getId(), movie.getId())) {
            throw new AppException(ErrorCode.MOVIE_REVIEW_ALREADY_EXISTS);
        }

        MovieReview review = MovieReview.builder()
                .movie(movie)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .createdAt(LocalDate.now())
                .build();

        return mapToResponse(movieReviewRepository.save(review));
    }

    @Override
    @Transactional
    public MovieReviewResponse updateReviewByAdmin(String reviewId, MovieReviewUpdateRequest request) {
        MovieReview review = movieReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_REVIEW_NOT_FOUND));

        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());

        return mapToResponse(movieReviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReviewByAdmin(String reviewId) {
        MovieReview review = movieReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_REVIEW_NOT_FOUND));
        movieReviewRepository.delete(review);
    }

    @Override
    public Page<MovieReviewResponse> getReviewsForAdmin(
            String movieId,
            Double minRating,
            Double maxRating,
            LocalDate fromDate,
            LocalDate toDate,
            String keyword,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        if (movieId != null && !movieId.trim().isEmpty()) {
            getMovieOrThrow(movieId.trim());
        }

        validateRatingRange(minRating, maxRating);
        validateDateRange(fromDate, toDate);

        PageRequest pageRequest = buildPageRequest(page, size, sortBy, sortDir);

        return movieReviewRepository.searchForAdmin(
                        normalizeToNull(movieId),
                        minRating,
                        maxRating,
                        fromDate,
                        toDate,
                        normalizeToNull(keyword),
                        pageRequest
                )
                .map(this::mapToResponse);
    }

    private User getCurrentUser(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return account.getUser();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Movie getMovieOrThrow(String movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
    }

    private void validateRatingRange(Double minRating, Double maxRating) {
        if (minRating != null && (minRating < 0 || minRating > 5)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "minRating phải từ 0 đến 5");
        }

        if (maxRating != null && (maxRating < 0 || maxRating > 5)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "maxRating phải từ 0 đến 5");
        }

        if (minRating != null && maxRating != null && minRating > maxRating) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Khoảng rating không hợp lệ");
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Khoảng thời gian không hợp lệ");
        }
    }

    private PageRequest buildPageRequest(int page, int size, String sortBy, String sortDir) {
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, normalizedSortBy));
    }

    private String normalizeSortBy(String sortBy) {
        if ("rating".equalsIgnoreCase(sortBy)) {
            return "rating";
        }
        return "createdAt";
    }

    private String normalizeToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private MovieReviewResponse mapToResponse(MovieReview review) {
        return MovieReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .movieId(review.getMovie().getId())
                .movieTitle(review.getMovie().getTitle())
                .userId(review.getUser().getId())
                .userFullName(review.getUser().getFullName())
                .build();
    }
}
