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
}