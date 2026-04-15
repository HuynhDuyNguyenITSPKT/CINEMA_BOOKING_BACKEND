package com.movie.cinema_booking_backend.service;

import java.util.List;

public interface IEmailService {
    void sendOtpEmail(String to, String otp);
    void sendGeneratedPasswordEmail(String to, String temporaryPassword);
    void sendAccountStatusChangedEmail(String to, String fullName, boolean isActive);
    void sendNewMovieNotificationEmail(String to, String movieTitle, String description, String releaseDate);
    void sendPaymentSuccessEmail(String to, String bookingId, String paymentMethod, String amount);
    void sendPaymentFailedEmail(String to, String bookingId, String paymentMethod, String amount, String reason);
    void sendTicketQrEmail(String to, String bookingId, String movieName, String amount, List<String> ticketIds);
}