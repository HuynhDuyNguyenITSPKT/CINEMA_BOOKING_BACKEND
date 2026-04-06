package com.movie.cinema_booking_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.movie.cinema_booking_backend.repository")
@EntityScan(basePackages = "com.movie.cinema_booking_backend.entity")
@EnableScheduling
@EnableAsync
public class CinemaBookingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinemaBookingBackendApplication.class, args);
    }

}
