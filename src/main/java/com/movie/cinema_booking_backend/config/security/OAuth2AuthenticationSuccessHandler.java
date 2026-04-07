package com.movie.cinema_booking_backend.config.security;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.enums.Role;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.service.IEmailService;
import com.movie.cinema_booking_backend.service.auth.OAuth2CodeExchangeService;
import com.movie.cinema_booking_backend.service.cache.UserAdminPageCache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        @Value("${app.frontend.oauth2.callback-url}")
        private String FRONTEND_OAUTH2_CALLBACK_URL;
        private final AccountRepository accountRepository;
        private final UserRepository userRepository;
        private final OAuth2CodeExchangeService oAuth2CodeExchangeService;
        private final IEmailService emailService;
        private final PasswordEncoder passwordEncoder;
        private final UserAdminPageCache userAdminPageCache;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                String email = oAuth2User.getAttribute("email");
                String fullName = oAuth2User.getAttribute("name");
                String phone = oAuth2User.getAttribute("phone");
                String ranPass = "Movieticker" + System.currentTimeMillis();
                // Tìm hoặc tự động tạo tài khoản
                Account account = accountRepository.findByUserEmail(email)
                                .orElseGet(() -> {
                                        User newUser = User.builder()
                                                        .email(email)
                                                        .fullName(fullName)
                                                        .phone(phone) // Mặc định do Google ko cung cấp trực tiếp
                                                        .dateOfBirth(LocalDate.of(2000, 1, 1))
                                                        .build();
                                        userRepository.save(newUser);

                                        Account newAccount = Account.builder()
                                                        .username(email)
                                                        .password(passwordEncoder.encode(ranPass)) // Đăng nhập OAuth2 không dùng password
                                                        .role(Role.USER)
                                                        .user(newUser)
                                                        .isActive(true)
                                                        .build();
                                        Account acc = accountRepository.save(newAccount);
                                        emailService.sendGeneratedPasswordEmail(email, ranPass);
                                        userAdminPageCache.clear();
                                        return acc;
                                });
                if(account.isActive() == false) {
                        String url = UriComponentsBuilder.fromUriString(FRONTEND_OAUTH2_CALLBACK_URL)
                                        .queryParam("error", "Tài khoản của bạn đã bị khóa.")
                                        .build()
                                        .encode()
                                        .toUriString();
                        getRedirectStrategy().sendRedirect(request, response, url);
                        return;
                }

                String exchangeCode = oAuth2CodeExchangeService.issueCode(account.getUsername());
                String callbackUrl = UriComponentsBuilder.fromUriString(FRONTEND_OAUTH2_CALLBACK_URL)
                                .queryParam("code", exchangeCode)
                                .build()
                                .encode()
                                .toUriString();
                getRedirectStrategy().sendRedirect(request, response, callbackUrl);
        }
}