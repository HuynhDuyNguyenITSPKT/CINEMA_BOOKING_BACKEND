package com.movie.cinema_booking_backend.config.security;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.enums.Role;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.service.auth.JwtTokenService;
import com.movie.cinema_booking_backend.service.auth.builder.AccessTokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptorDirector;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        @Value("${app.frontend.oauth2.callback-url}")
        private String FRONTEND_OAUTH2_CALLBACK_URL;
        private final AccountRepository accountRepository;
        private final UserRepository userRepository;
        private final JwtTokenService jwtTokenService;
        private final TokenDescriptorDirector tokenDescriptorDirector;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                String email = oAuth2User.getAttribute("email");
                String fullName = oAuth2User.getAttribute("name");
                String phone = oAuth2User.getAttribute("phone");

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

                                        String ranPass = "oauth2user" + System.currentTimeMillis();
                                        Account newAccount = Account.builder()
                                                        .username(email)
                                                        .password(ranPass) // Đăng nhập OAuth2 không dùng password
                                                        .role(Role.USER)
                                                        .user(newUser)
                                                        .isActive(true)
                                                        .build();
                                        return accountRepository.save(newAccount);
                                });
                if(account.isActive() == false) {
                        System.out.println("url: " + FRONTEND_OAUTH2_CALLBACK_URL + "?error=Tài khoản của bạn đã bị khóa.");
                        String errorMessage = URLEncoder.encode("Tài khoản của bạn đã bị khóa.", StandardCharsets.UTF_8.toString());
                        String url = "http://localhost:5173/oauth2/callback?error=" + errorMessage;
                        getRedirectStrategy().sendRedirect(request, response, url);
                        return;
                }
                // Tạo JWT Token
                TokenDescriptorBuilder accessToken = new AccessTokenDescriptorBuilder(jwtTokenService);
                TokenDescriptorBuilder refreshToken = new AccessTokenDescriptorBuilder(jwtTokenService);

                tokenDescriptorDirector.makeAccessToken(accessToken, account.getUsername(), account.getRole().name());
                tokenDescriptorDirector.makeRefreshToken(refreshToken, account.getUsername(), account.getRole().name());
                String accessTokenStr = accessToken.getResult().getToken();
                String refreshTokenStr = refreshToken.getResult().getToken();

                        // Redirect về Frontend kèm Token trên URL
                        String targetUrl = UriComponentsBuilder.fromUriString(FRONTEND_OAUTH2_CALLBACK_URL)
                                        .queryParam("accessToken", accessTokenStr)
                                        .queryParam("refreshToken", refreshTokenStr)
                                        .build().toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
}