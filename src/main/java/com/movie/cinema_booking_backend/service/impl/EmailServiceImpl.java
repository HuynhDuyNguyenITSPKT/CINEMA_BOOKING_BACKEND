package com.movie.cinema_booking_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.service.IEmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Ma OTP xac thuc - Cinema Booking");
        message.setText("Chao ban,\n\nMa OTP cua ban la: " + otp
                + "\nMa nay co hieu luc trong 5 phut. Vui long khong chia se ma nay cho bat ky ai.");
        mailSender.send(message);
    }

    @Override
    public void sendNewMovieNotificationEmail(String to, String movieTitle, String description, String releaseDate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("[Cinema Booking] Phim moi ra mat: " + movieTitle);
        message.setText(
            "Chao ban,\n\n" +
            "Chung toi vui mung thong bao phim moi vua duoc them vao he thong:\n\n" +
            "Ten phim : " + movieTitle + "\n" +
            "Ngay cong chieu: " + releaseDate + "\n" +
            "Mo ta     : " + description + "\n\n" +
            "Truy cap Cinema Booking de dat ve ngay!\n\n" +
            "Tran trong,\nDoi ngu Cinema Booking"
        );
        mailSender.send(message);
    }

    @Override
    public void sendMovieUpdatedNotificationEmail(String to, String movieTitle, String description) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("[Cinema Booking] Thong tin phim duoc cap nhat: " + movieTitle);
        message.setText(
            "Chao ban,\n\n" +
            "Thong tin phim \"" + movieTitle + "\" vua duoc cap nhat:\n\n" +
            "Mo ta moi: " + description + "\n\n" +
            "Truy cap Cinema Booking de xem chi tiet.\n\n" +
            "Tran trong,\nDoi ngu Cinema Booking"
        );
        mailSender.send(message);
    }

    @Override
    public void sendMovieDeletedNotificationEmail(String to, String movieTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("[Cinema Booking] Phim da bi go: " + movieTitle);
        message.setText(
            "Chao ban,\n\n" +
            "Phim \"" + movieTitle + "\" da duoc go khoi he thong Cinema Booking.\n" +
            "Neu ban da dat ve, vui long lien he de duoc ho tro.\n\n" +
            "Tran trong,\nDoi ngu Cinema Booking"
        );
        mailSender.send(message);
    }
}