package com.movie.cinema_booking_backend.service.auth.login.strategy;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.enums.Role;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoogleLoginStrategy implements LoginStrategy {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;

    @Value("${app.auth.google.client-id:}")
    private String googleClientId;

    @Override
    public Account authenticate(Object request) {
        String tokenId = request instanceof String ? (String) request : null;
        if (tokenId == null || tokenId.isBlank() || googleClientId == null || googleClientId.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenId);
            if (idToken == null) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            GoogleIdToken.Payload tokenPayload = idToken.getPayload();
            String email = tokenPayload.getEmail();
            String fullName = (String) tokenPayload.get("name");

            if (email == null || email.isBlank() || !Boolean.TRUE.equals(tokenPayload.getEmailVerified())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .fullName(fullName == null || fullName.isBlank() ? email : fullName)
                        .phone(null)
                        .dateOfBirth(LocalDate.now())
                        .build();
                return userRepository.save(newUser);
            });

            Account account = accountRepository.findByUserEmail(email).orElseGet(() -> {
                String rawPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                Account newAccount = Account.builder()
                        .username(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .user(user)
                        .role(Role.USER)
                        .isActive(true)
                        .build();
                Account savedAccount = accountRepository.save(newAccount);
                emailService.sendGeneratedPasswordEmail(email, rawPassword);
                return savedAccount;
            });

            if (!account.isActive()) {
                throw new AppException(ErrorCode.ACCOUNT_LOCKED);
            }

            return account;
        } catch (GeneralSecurityException | IOException ex) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
}
