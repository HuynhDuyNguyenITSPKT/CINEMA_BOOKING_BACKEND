package com.movie.cinema_booking_backend.config.security;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import com.movie.cinema_booking_backend.repository.InvalidatedTokenRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${security.jwt.signerKey}")
    private String signerKey;

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            MACVerifier verifier = new MACVerifier(signerKey.getBytes());
            if (!signedJWT.verify(verifier)) {
                throw new JwtException("Invalid token signature");
            }

            String type = (String) signedJWT.getJWTClaimsSet().getClaim("type");
            if (!"ACCESS".equals(type)) {
                throw new JwtException("Chỉ Access Token mới có quyền truy cập API này");
            }

            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiryTime == null || expiryTime.before(new Date())) {
                throw new JwtException("Token has expired or is invalid");
            }

            String jit = signedJWT.getJWTClaimsSet().getJWTID();
            if (jit != null && invalidatedTokenRepository.existsById(jit)) {
                throw new JwtException("Token has been invalidated");
            }

            Instant issueTime = signedJWT.getJWTClaimsSet().getIssueTime() != null
                    ? signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                    : Instant.now(); 

            return new Jwt(
                    token,
                    issueTime,
                    expiryTime.toInstant(),
                    signedJWT.getHeader().toJSONObject(),
                    signedJWT.getJWTClaimsSet().getClaims());

        } catch (ParseException | JOSEException e) {
            throw new JwtException("Invalid token format");
        } catch (Exception e) {
            throw new JwtException("Error decoding token: " + e.getMessage());
        }
    }

}
