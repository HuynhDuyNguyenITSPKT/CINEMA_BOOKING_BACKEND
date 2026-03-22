package com.movie.cinema_booking_backend.service.impl;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.InvalidatedToken;
import com.movie.cinema_booking_backend.entity.PendingRegistration;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.enums.Role;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.InvalidatedTokenRepository;
import com.movie.cinema_booking_backend.repository.PendingRegistrationRepository;
import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.request.AuthRequest;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.service.IAuthService;
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

import jakarta.transaction.Transactional;

@Service
public class AuthService implements IAuthService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRepo;
    private final InvalidatedTokenRepository invalidatedRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AccountRepository accountRepository, UserRepository userRepository, PendingRegistrationRepository pendingRepo, InvalidatedTokenRepository invalidatedRepo, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.pendingRepo = pendingRepo;
        this.invalidatedRepo = invalidatedRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${security.jwt.signerKey}")
    private String signerKey;

    @Override
    @Transactional
    public void register(RegistrationRequest request) {

        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTS);
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXISTS);
        }

        if (request.getDateOfBirth().isAfter(java.time.LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_DATE_OF_BIRTH);
        }
        
        pendingRepo.deleteByEmail(request.getEmail());
        pendingRepo.deleteByUsername(request.getUsername());

        String otp = String.format("%06d", new Random().nextInt(999999));
        PendingRegistration pending = PendingRegistration.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail()).fullName(request.getFullName())
                .phone(request.getPhone()).dateOfBirth(request.getDateOfBirth())
                .otp(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .otpGeneratedTime(LocalDateTime.now())
                .build();
        pendingRepo.save(pending);

        System.out.println("OTP for " + request.getUsername() + ": " + otp);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp){
       
        PendingRegistration pending = pendingRepo.findByEmail(email);

        if (pending == null) {
            throw new AppException(ErrorCode.PENDING_REGISTRATION_NOT_FOUND);
        }

        if (!pending.getOtp().equals(otp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        
        if (pending.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        User user = User.builder()
                .fullName(pending.getFullName())
                .email(pending.getEmail())
                .phone(pending.getPhone())
                .dateOfBirth(pending.getDateOfBirth())
                .build();
        User savedUser = userRepository.save(user);

        Account account = Account.builder()
                .username(pending.getUsername())
                .password(pending.getPassword()) 
                .role(Role.USER) 
                .user(savedUser)
                .isActive(true)
                .build();
        accountRepository.save(account);

        pendingRepo.delete(pending);
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        PendingRegistration pending = pendingRepo.findByEmail(email);
        if (pending == null) {
            throw new AppException(ErrorCode.PENDING_REGISTRATION_NOT_FOUND);
        }
        String newOtp = String.format("%06d", new Random().nextInt(999999));
        pending.setOtp(newOtp);
        pending.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        pending.setOtpGeneratedTime(LocalDateTime.now());
        pendingRepo.save(pending);
        System.out.println("Resent OTP for " + pending.getUsername() + ": " + newOtp);
        // emailService.sendOtpEmail(email, newOtp);
    }

    @Override
    public AuthResponse login(AuthRequest req) {
        Account acc = accountRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(req.getPassword(), acc.getPassword()))
            throw new AppException(ErrorCode.INVALID_PASSWORD);

        return buildAuthResponse(acc);
    }

    @Override
    public void logout(String token) throws ParseException {
        var signedJWT = SignedJWT.parse(token);
        invalidatedRepo.save(new InvalidatedToken(
                signedJWT.getJWTClaimsSet().getJWTID(), 
                signedJWT.getJWTClaimsSet().getExpirationTime()
        ));
    }

    @Override
    public AuthResponse refreshToken(String token) throws Exception {
        var signedJWT = verifyToken(token); 
        String type = signedJWT.getJWTClaimsSet().getStringClaim("type");
        if (!"REFRESH".equals(type)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        logout(token); 
        Account acc = accountRepository.findByUsername(signedJWT.getJWTClaimsSet().getSubject()).orElseThrow();
        return buildAuthResponse(acc);
    }

    private AuthResponse buildAuthResponse(Account acc) {
        return AuthResponse.builder()
                .accessToken(generateToken(acc, 3600, "ACCESS"))
                .refreshToken(generateToken(acc, 604800, "REFRESH"))
                .build();
    }

    private String generateToken(Account acc, long duration,String type) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(acc.getUsername())
                .expirationTime(new Date(System.currentTimeMillis() + duration * 1000))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", acc.getRole().name())
                .claim("type", type)
                .build();
        JWSObject jws = new JWSObject(header, new Payload(claims.toJSONObject()));
        try {
            jws.sign(new MACSigner(signerKey.getBytes()));
            return jws.serialize();
        } catch (JOSEException e) { throw new RuntimeException(e); }
    }

    public SignedJWT verifyToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        if (!signedJWT.verify(verifier) || signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date()))
            throw new RuntimeException("Invalid or expired token");
        if (invalidatedRepo.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new RuntimeException("Token revoked");
        return signedJWT;
    }
}
