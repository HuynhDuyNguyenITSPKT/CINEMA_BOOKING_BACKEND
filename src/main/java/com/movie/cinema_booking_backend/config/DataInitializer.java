package com.movie.cinema_booking_backend.config;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.enums.Role;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Bean
    ApplicationRunner initData(AccountRepository accountRepository, UserRepository userRepository) {
        return args -> {

            String adminUsername = "admin";

            if (accountRepository.existsByUsername(adminUsername)) {
                log.info("Admin account already exists. Skipping initialization.");
                return;
            }

            User adminUser = User.builder()
                    .fullName("System Administrator")
                    .email("admin@movieticker.com")
                    .phone("0000000000")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .build();
            
            User savedUser = userRepository.save(adminUser);

            Account adminAccount = Account.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode("admin123")) 
                    .role(Role.ADMIN)
                    .user(savedUser)
                    .isActive(true)
                    .build();

            accountRepository.save(adminAccount);
            
            log.info("--- Default Admin Account Created ---");
            log.info("Username: {}", adminUsername);
            log.info("Password: admin123");
            log.info("-------------------------------------");
            
        };
    }
}