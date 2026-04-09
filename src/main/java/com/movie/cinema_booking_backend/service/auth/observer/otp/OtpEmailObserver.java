package com.movie.cinema_booking_backend.service.auth.observer.otp;

import com.movie.cinema_booking_backend.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpEmailObserver implements OtpObserver {

    private final IEmailService emailService;

    @Override
    public void onOtpGenerated(String email, String otp) {
        emailService.sendOtpEmail(email, otp);
    }
}