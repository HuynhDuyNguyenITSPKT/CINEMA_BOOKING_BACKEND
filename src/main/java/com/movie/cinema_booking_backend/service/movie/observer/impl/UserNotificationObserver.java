package com.movie.cinema_booking_backend.service.movie.observer.impl;

import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.response.MovieResponse;
import com.movie.cinema_booking_backend.service.IEmailService;
import com.movie.cinema_booking_backend.service.movie.observer.IMovieObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationObserver implements IMovieObserver {

    private final IEmailService emailService;
    private final UserRepository userRepository;

    @Override
    @Async
    public void onMovieAdded(MovieResponse movie) {
        log.info("[UserNotificationObserver] Phim moi: {} — bat dau gui email thong bao.", movie.getTitle());
        List<String> emails = userRepository.findAllEmails();
        String releaseDate = movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : "Chua xac dinh";
        String description = movie.getDescription() != null ? movie.getDescription() : "";
        for (String email : emails) {
            try {
                emailService.sendNewMovieNotificationEmail(email, movie.getTitle(), description, releaseDate);
                log.debug("[UserNotificationObserver] Da gui email 'phim moi' toi: {}", email);
            } catch (Exception e) {
                log.error("[UserNotificationObserver] Gui email that bai toi {}: {}", email, e.getMessage());
            }
        }
        log.info("[UserNotificationObserver] Hoan thanh gui thong bao phim moi cho {} nguoi dung.", emails.size());
    }

    @Override
    @Async
    public void onMovieUpdated(MovieResponse movie) {
        log.info("[UserNotificationObserver] Phim cap nhat: {} — bat dau gui email thong bao.", movie.getTitle());
        List<String> emails = userRepository.findAllEmails();
        String description = movie.getDescription() != null ? movie.getDescription() : "";
        for (String email : emails) {
            try {
                emailService.sendMovieUpdatedNotificationEmail(email, movie.getTitle(), description);
                log.debug("[UserNotificationObserver] Da gui email 'cap nhat phim' toi: {}", email);
            } catch (Exception e) {
                log.error("[UserNotificationObserver] Gui email that bai toi {}: {}", email, e.getMessage());
            }
        }
        log.info("[UserNotificationObserver] Hoan thanh gui thong bao cap nhat phim cho {} nguoi dung.", emails.size());
    }

    @Override
    @Async
    public void onMovieDeleted(String movieId) {
        log.info("[UserNotificationObserver] Phim bi xoa — ID: {} — bat dau gui email thong bao.", movieId);
        List<String> emails = userRepository.findAllEmails();
        for (String email : emails) {
            try {
                emailService.sendMovieDeletedNotificationEmail(email, movieId);
                log.debug("[UserNotificationObserver] Da gui email 'phim bi xoa' toi: {}", email);
            } catch (Exception e) {
                log.error("[UserNotificationObserver] Gui email that bai toi {}: {}", email, e.getMessage());
            }
        }
        log.info("[UserNotificationObserver] Hoan thanh gui thong bao xoa phim cho {} nguoi dung.", emails.size());
    }
}
