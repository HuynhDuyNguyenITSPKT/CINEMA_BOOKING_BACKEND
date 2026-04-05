package com.movie.cinema_booking_backend.service;

public interface IEmailService {
    void sendOtpEmail(String to, String otp);
    void sendGeneratedPasswordEmail(String to, String temporaryPassword);
    void sendAccountStatusChangedEmail(String to, String fullName, boolean isActive);
    void sendNewMovieNotificationEmail(String to, String movieTitle, String description, String releaseDate);
    void sendMovieUpdatedNotificationEmail(String to, String movieTitle, String description);
    void sendMovieDeletedNotificationEmail(String to, String movieTitle);
    void sendPaymentSuccessEmail(String to, String bookingId, String paymentMethod, String amount);
    void sendPaymentFailedEmail(String to, String bookingId, String paymentMethod, String amount, String reason);
}