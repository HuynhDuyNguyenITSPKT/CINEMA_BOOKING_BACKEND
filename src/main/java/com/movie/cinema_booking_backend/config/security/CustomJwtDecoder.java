package com.movie.cinema_booking_backend.config.security;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import com.movie.cinema_booking_backend.service.auth.JwtTokenService;
import com.nimbusds.jwt.SignedJWT;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtTokenService jwtTokenService;

    public CustomJwtDecoder(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = jwtTokenService.verifyToken(token);

            String type = (String) signedJWT.getJWTClaimsSet().getClaim("type");
            if (!"ACCESS".equals(type)) {
                throw new JwtException("Chỉ Access Token mới có quyền truy cập API này");
            }

            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            Instant issueTime = signedJWT.getJWTClaimsSet().getIssueTime() != null
                    ? signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                    : Instant.now(); 

            return new Jwt(
                    token,
                    issueTime,
                    expiryTime.toInstant(),
                    signedJWT.getHeader().toJSONObject(),
                    signedJWT.getJWTClaimsSet().getClaims());

        } catch (ParseException e) {
            throw new JwtException("Invalid token format");
        } catch (Exception e) {
            throw new JwtException("Error decoding token: " + e.getMessage());
        }
    }

}
