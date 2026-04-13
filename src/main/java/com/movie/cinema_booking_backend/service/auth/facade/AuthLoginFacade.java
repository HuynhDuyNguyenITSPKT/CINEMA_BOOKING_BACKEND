package com.movie.cinema_booking_backend.service.auth.facade;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.service.auth.JwtTokenService;
import com.movie.cinema_booking_backend.service.auth.builder.AccessTokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.RefreshTokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptor;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptorDirector;
import com.movie.cinema_booking_backend.service.auth.login.LoginContext;
import com.movie.cinema_booking_backend.service.auth.login.strategy.GoogleLoginStrategy;
import com.movie.cinema_booking_backend.service.auth.login.strategy.PasswordLoginStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthLoginFacade {

    private final JwtTokenService jwtTokenService;
    private final TokenDescriptorDirector tokenDescriptorDirector;
    private final PasswordLoginStrategy passwordLoginStrategy;
    private final GoogleLoginStrategy googleLoginStrategy;

    public AuthResponse login(String type, Object request) {
        LoginContext context = new LoginContext();
        if ("google".equals(type)) {
            context.setStrategy(googleLoginStrategy);
        } else {
            context.setStrategy(passwordLoginStrategy);
        }
        Account account = context.authenticate(request);
        return issueAuthTokens(account);
    }

    public AuthResponse issueAuthTokens(Account account) {
        TokenDescriptorBuilder accessBuilder = new AccessTokenDescriptorBuilder(jwtTokenService);
        TokenDescriptorBuilder refreshBuilder = new RefreshTokenDescriptorBuilder(jwtTokenService);

        tokenDescriptorDirector.makeAccessToken(accessBuilder, account.getUsername(), account.getRole().name());
        tokenDescriptorDirector.makeRefreshToken(refreshBuilder, account.getUsername(), account.getRole().name());

        TokenDescriptor accessDescriptor = accessBuilder.getResult();
        TokenDescriptor refreshDescriptor = refreshBuilder.getResult();

        return AuthResponse.builder()
                .accessToken(accessDescriptor.getToken())
                .refreshToken(refreshDescriptor.getToken())
                .build();
    }
}
