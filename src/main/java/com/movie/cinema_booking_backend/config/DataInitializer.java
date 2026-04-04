package com.movie.cinema_booking_backend.config;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.enums.Role;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    @Value("${ADMIN_FULL_NAME:System Administrator}")
    private String adminFullName;

    @Value("${ADMIN_EMAIL:admin@movieticker.com}")
    private String adminEmail;

    @Value("${ADMIN_PHONE:0000000000}")
    private String adminPhone;

    @Value("${ADMIN_BIRTH_DATE:1990-01-01}")
    private String adminBirthDate;

    @Bean
    ApplicationRunner initData(AccountRepository accountRepository, UserRepository userRepository) {
        return args -> {

            if (accountRepository.existsByUsername(adminUsername)) {
                log.info("Admin account already exists. Skipping initialization.");
                return;
            }

            User adminUser = User.builder()
                    .fullName(adminFullName)
                    .email(adminEmail)
                    .phone(adminPhone)
                    .dateOfBirth(LocalDate.parse(adminBirthDate))
                    .build();
            
            User savedUser = userRepository.save(adminUser);

            Account adminAccount = Account.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .user(savedUser)
                    .isActive(true)
                    .build();

            accountRepository.save(adminAccount);
            
            log.info("--- Default Admin Account Created ---");
            log.info("Username: {}", adminUsername);
            log.info("Password: [configured via ADMIN_PASSWORD]");
            log.info("-------------------------------------");
            
        };
    }
}