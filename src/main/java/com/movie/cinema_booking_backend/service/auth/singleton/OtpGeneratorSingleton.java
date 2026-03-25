package com.movie.cinema_booking_backend.service.auth.singleton;

import java.util.Random;

public class OtpGeneratorSingleton {

    private static volatile OtpGeneratorSingleton instance;

    private final Random random = new Random();

    private OtpGeneratorSingleton() {
    }

    public static OtpGeneratorSingleton getInstance() {
        if (instance == null) {
            synchronized (OtpGeneratorSingleton.class) {
                if (instance == null) {
                    instance = new OtpGeneratorSingleton();
                }
            }
        }
        return instance;
    }

    public String generateSixDigits() {
        return String.format("%06d", random.nextInt(999999));
    }
}
