package com.movie.cinema_booking_backend.service.impl;

import java.text.ParseException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
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
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IAuthService;
import com.movie.cinema_booking_backend.service.auth.JwtTokenService;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptorDirector;
import com.movie.cinema_booking_backend.service.auth.singleton.OtpGeneratorSingleton;
import com.nimbusds.jwt.SignedJWT;

import jakarta.transaction.Transactional;

@Service
public class AuthService implements IAuthService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRepo;
    private final InvalidatedTokenRepository invalidatedRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final TokenDescriptorDirector tokenDescriptorDirector;

    public AuthService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            PendingRegistrationRepository pendingRepo,
            InvalidatedTokenRepository invalidatedRepo,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            TokenDescriptorDirector tokenDescriptorDirector
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.pendingRepo = pendingRepo;
        this.invalidatedRepo = invalidatedRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.tokenDescriptorDirector = tokenDescriptorDirector;
    }

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

        String otp =  OtpGeneratorSingleton.getInstance().generateSixDigits();
        LocalDateTime now = LocalDateTime.now();
        PendingRegistration pending = PendingRegistration.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .otp(otp)
                .expiryDate(now.plusMinutes(5))
                .otpGeneratedTime(now)
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
        String newOtp = OtpGeneratorSingleton.getInstance().generateSixDigits();
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
        invalidatedRepo.save(InvalidatedToken.builder()
                .id(signedJWT.getJWTClaimsSet().getJWTID())
                .expiryDate(signedJWT.getJWTClaimsSet().getExpirationTime())
                .build());
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

    @Override
    public UserResponse getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        Account acc = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User user = acc.getUser();
        return UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .role(acc.getRole())
                .build();
    }

    private AuthResponse buildAuthResponse(Account acc) {
        return AuthResponse.builder()
                .accessToken(jwtTokenService.generateToken(acc, tokenDescriptorDirector.buildAccessDescriptor()))
                .refreshToken(jwtTokenService.generateToken(acc, tokenDescriptorDirector.buildRefreshDescriptor()))
                .build();
    }

    public SignedJWT verifyToken(String token) throws Exception {
        return jwtTokenService.verifyToken(token);
    }
}
