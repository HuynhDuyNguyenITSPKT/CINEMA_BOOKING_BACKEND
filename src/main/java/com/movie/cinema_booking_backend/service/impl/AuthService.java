package com.movie.cinema_booking_backend.service.impl;

import java.text.ParseException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.InvalidatedToken;
import com.movie.cinema_booking_backend.entity.PendingRegistration;
import com.movie.cinema_booking_backend.entity.PendingPasswordReset;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.InvalidatedTokenRepository;
import com.movie.cinema_booking_backend.repository.PendingPasswordResetRepository;
import com.movie.cinema_booking_backend.repository.PendingRegistrationRepository;
import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.request.AuthRequest;
import com.movie.cinema_booking_backend.request.ChangePasswordRequest;
import com.movie.cinema_booking_backend.request.ForgotPasswordRequest;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.request.ResetPasswordRequest;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IAuthService;
import com.movie.cinema_booking_backend.service.auth.JwtTokenService;
import com.movie.cinema_booking_backend.service.auth.factory.AuthEntityFactory;
import com.movie.cinema_booking_backend.service.auth.factory.concrete.AccountFactory;
import com.movie.cinema_booking_backend.service.auth.factory.concrete.PendingPasswordResetFactory;
import com.movie.cinema_booking_backend.service.auth.factory.concrete.PendingRegistrationFactory;
import com.movie.cinema_booking_backend.service.auth.factory.concrete.UserFactory;
import com.movie.cinema_booking_backend.service.auth.builder.AccessTokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.RefreshTokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptor;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptorBuilder;
import com.movie.cinema_booking_backend.service.auth.builder.TokenDescriptorDirector;
import com.movie.cinema_booking_backend.service.auth.singleton.OtpGeneratorSingleton;
import com.nimbusds.jwt.SignedJWT;

import jakarta.transaction.Transactional;

@Service
public class AuthService implements IAuthService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRepo;
    private final PendingPasswordResetRepository pendingPasswordResetRepository;
    private final InvalidatedTokenRepository invalidatedRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final TokenDescriptorDirector tokenDescriptorDirector;

    public AuthService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            PendingRegistrationRepository pendingRepo,
            PendingPasswordResetRepository pendingPasswordResetRepository,
            InvalidatedTokenRepository invalidatedRepo,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            TokenDescriptorDirector tokenDescriptorDirector
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.pendingRepo = pendingRepo;
        this.pendingPasswordResetRepository = pendingPasswordResetRepository;
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
        PendingRegistration pending = AuthEntityFactory.getEntity(
            new PendingRegistrationFactory(
                request,
                passwordEncoder.encode(request.getPassword()),
                otp,
                now
            )
        );
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

        User user = AuthEntityFactory.getEntity(new UserFactory(pending));
        User savedUser = userRepository.save(user);

        Account account = AuthEntityFactory.getEntity(new AccountFactory(pending, savedUser));
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
        if (!acc.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
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

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        pendingPasswordResetRepository.deleteByEmail(user.getEmail());

        String otp = OtpGeneratorSingleton.getInstance().generateSixDigits();
        LocalDateTime now = LocalDateTime.now();

        PendingPasswordReset pending = AuthEntityFactory.getEntity(
            new PendingPasswordResetFactory(user.getEmail(), otp, now)
        );

        pendingPasswordResetRepository.save(pending);
        System.out.println("Reset password OTP for " + user.getEmail() + ": " + otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PendingPasswordReset pending = pendingPasswordResetRepository.findByEmail(request.getEmail());
        if (pending == null) {
            throw new AppException(ErrorCode.PENDING_RESET_PASSWORD_NOT_FOUND);
        }

        if (!pending.getOtp().equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (pending.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        Account account = userRepository.findByEmail(request.getEmail())
                .map(User::getAccount)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getNewPassword().length() < 6) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        pendingPasswordResetRepository.delete(pending);
    }

    @Override
    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        if (request.getNewPassword().length() < 6) {
            throw new AppException(ErrorCode.PASSWORD_INVALID);
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    private AuthResponse buildAuthResponse(Account acc) {
        TokenDescriptorBuilder accessBuilder = new AccessTokenDescriptorBuilder(jwtTokenService);
        TokenDescriptorBuilder refreshBuilder = new RefreshTokenDescriptorBuilder(jwtTokenService);

        tokenDescriptorDirector.makeAccessToken(accessBuilder, acc.getUsername(), acc.getRole().name());
        tokenDescriptorDirector.makeRefreshToken(refreshBuilder, acc.getUsername(), acc.getRole().name());

        TokenDescriptor accessDescriptor = accessBuilder.getResult();
        TokenDescriptor refreshDescriptor = refreshBuilder.getResult();

        return AuthResponse.builder()
            .accessToken(accessDescriptor.getToken())
            .refreshToken(refreshDescriptor.getToken())
                .build();
    }

    public SignedJWT verifyToken(String token) throws Exception {
        return jwtTokenService.verifyToken(token);
    }
}
