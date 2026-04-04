package com.movie.cinema_booking_backend.service;

public interface IEmailService {
    void sendOtpEmail(String to, String otp);
    void sendNewMovieNotificationEmail(String to, String movieTitle, String description, String releaseDate);
    void sendMovieUpdatedNotificationEmail(String to, String movieTitle, String description);
    void sendMovieDeletedNotificationEmail(String to, String movieTitle);
}