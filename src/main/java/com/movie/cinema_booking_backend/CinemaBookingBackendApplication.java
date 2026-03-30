package com.movie.cinema_booking_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CinemaBookingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinemaBookingBackendApplication.class, args);
    }

}
