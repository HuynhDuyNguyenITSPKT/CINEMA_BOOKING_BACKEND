package com.movie.cinema_booking_backend.service.auth;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.InvalidatedTokenRepository;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptor;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtTokenService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Value("${security.jwt.signerKey}")
    private String signerKey;

    public JwtTokenService(InvalidatedTokenRepository invalidatedTokenRepository) {
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    public String generateToken(Account account, TokenDescriptor descriptor) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(account.getUsername())
                .expirationTime(new Date(System.currentTimeMillis() + descriptor.getDurationSeconds() * 1000))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", account.getRole().name())
                .claim("type", descriptor.getType())
                .build();
        JWSObject jws = new JWSObject(header, new Payload(claims.toJSONObject()));
        try {
            jws.sign(new MACSigner(signerKey.getBytes()));
            return jws.serialize();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public SignedJWT verifyToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        if (!signedJWT.verify(verifier) || signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.TOKEN_REVOKED);
        }
        return signedJWT;
    }
}
