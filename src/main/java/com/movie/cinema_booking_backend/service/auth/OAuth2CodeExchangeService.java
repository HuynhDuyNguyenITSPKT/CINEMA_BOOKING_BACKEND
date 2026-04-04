package com.movie.cinema_booking_backend.service.auth;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.service.auth.builder.AccessTokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.RefreshTokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptor;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptorDirector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OAuth2CodeExchangeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_BYTES = 24;

    @Value("${app.auth.oauth2-code.ttl-seconds:60}")
    private long codeTtlSeconds;

    private final AccountRepository accountRepository;
    private final JwtTokenService jwtTokenService;
    private final TokenDescriptorDirector tokenDescriptorDirector;

    private final Map<String, PendingCode> pendingCodes = new ConcurrentHashMap<>();

    public OAuth2CodeExchangeService(
            AccountRepository accountRepository,
            JwtTokenService jwtTokenService,
            TokenDescriptorDirector tokenDescriptorDirector
    ) {
        this.accountRepository = accountRepository;
        this.jwtTokenService = jwtTokenService;
        this.tokenDescriptorDirector = tokenDescriptorDirector;
    }

    public String issueCode(String username) {
        purgeExpiredCodes();
        String code = generateCode();
        Instant expiresAt = Instant.now().plusSeconds(codeTtlSeconds);
        pendingCodes.put(code, new PendingCode(username, expiresAt));
        return code;
    }

    public AuthResponse exchangeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new AppException(ErrorCode.OAUTH2_CODE_INVALID);
        }

        PendingCode pendingCode = pendingCodes.remove(code);
        if (pendingCode == null) {
            throw new AppException(ErrorCode.OAUTH2_CODE_INVALID);
        }

        if (pendingCode.expiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.OAUTH2_CODE_EXPIRED);
        }

        Account account = accountRepository.findByUsername(pendingCode.username())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!account.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        return buildAuthResponse(account);
    }

    private AuthResponse buildAuthResponse(Account account) {
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

    private void purgeExpiredCodes() {
        Instant now = Instant.now();
        pendingCodes.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private String generateCode() {
        byte[] randomBytes = new byte[CODE_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private record PendingCode(String username, Instant expiresAt) {
    }
}
