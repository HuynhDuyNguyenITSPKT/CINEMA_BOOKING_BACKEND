package com.movie.cinema_booking_backend.service.impl;

import java.text.ParseException;
import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
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
import com.movie.cinema_booking_backend.request.ChangePasswordRequest;
import com.movie.cinema_booking_backend.request.ForgotPasswordRequest;
import com.movie.cinema_booking_backend.request.RegistrationRequest;
import com.movie.cinema_booking_backend.request.ResetPasswordRequest;
import com.movie.cinema_booking_backend.response.AuthResponse;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IAuthService;
import com.movie.cinema_booking_backend.service.auth.JwtTokenService;
import com.movie.cinema_booking_backend.service.auth.facade.AuthLoginFacade;
import com.movie.cinema_booking_backend.service.auth.factory.AuthEntityFactory;
import com.movie.cinema_booking_backend.service.auth.factory.concrete.AccountFactory;
import com.movie.cinema_booking_backend.service.auth.factory.concrete.PendingPasswordResetFactory;
import com.movie.cinema_booking_backend.service.auth.factory.concrete.PendingRegistrationFactory;
import com.movie.cinema_booking_backend.service.auth.factory.concrete.UserFactory;
import com.movie.cinema_booking_backend.service.auth.observer.otp.OtpEventPublisher;
import com.movie.cinema_booking_backend.service.auth.singleton.OtpGeneratorSingleton;
import com.movie.cinema_booking_backend.service.user.cache.UserAdminPageCache;
import com.nimbusds.jwt.SignedJWT;

import jakarta.transaction.Transactional;

@Service
public class AuthServiceImpl implements IAuthService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRepo;
    private final PendingPasswordResetRepository pendingPasswordResetRepository;
    private final InvalidatedTokenRepository invalidatedRepo;
    private final PasswordEncoder passwordEncoder;
    private final OtpEventPublisher otpEventPublisher;
    private final JwtTokenService jwtTokenService;
    private final AuthLoginFacade authLoginFacade;
    private final UserAdminPageCache userAdminPageCache;

    public AuthServiceImpl(
            AccountRepository accountRepository,
            UserRepository userRepository,
            PendingRegistrationRepository pendingRepo,
            PendingPasswordResetRepository pendingPasswordResetRepository,
            InvalidatedTokenRepository invalidatedRepo,
            PasswordEncoder passwordEncoder,
            OtpEventPublisher otpEventPublisher,
            JwtTokenService jwtTokenService,
            AuthLoginFacade authLoginFacade,
            UserAdminPageCache userAdminPageCache
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.pendingRepo = pendingRepo;
        this.pendingPasswordResetRepository = pendingPasswordResetRepository;
        this.invalidatedRepo = invalidatedRepo;
        this.passwordEncoder = passwordEncoder;
        this.otpEventPublisher = otpEventPublisher;
        this.jwtTokenService = jwtTokenService;
        this.authLoginFacade = authLoginFacade;
        this.userAdminPageCache = userAdminPageCache;
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

        otpEventPublisher.notifyOtpGenerated(request.getEmail(), otp);
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
        userAdminPageCache.clear();

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
        otpEventPublisher.notifyOtpGenerated(email, newOtp);
    }

    @Override
    @Transactional
    public AuthResponse login(String type, Object request) {
        return authLoginFacade.login(type, request);
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
        return authLoginFacade.issueAuthTokens(acc);
    }

    @Override
    public UserResponse getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        Account acc = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User user = acc.getUser();
        return UserResponse.builder()
                .id(user.getId().toString())
                .username(acc.getUsername())
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

        String otp = OtpGeneratorSingleton.getInstance().generateSixDigits();
        LocalDateTime now = LocalDateTime.now();
        String email = user.getEmail();

        PendingPasswordReset pending = pendingPasswordResetRepository.findByEmail(email);
        if (pending == null) {
            pending = AuthEntityFactory.getEntity(
                new PendingPasswordResetFactory(email, otp, now)
            );
        } else {
            pending.setOtp(otp);
            pending.setOtpGeneratedTime(now);
            pending.setExpiryDate(now.plusMinutes(5));
        }

        try {
            pendingPasswordResetRepository.save(pending);
        } catch (DataIntegrityViolationException ex) {
            // Handle concurrent forgot-password requests for the same email.
            PendingPasswordReset existingPending = pendingPasswordResetRepository.findByEmail(email);
            if (existingPending == null) {
                throw ex;
            }
            existingPending.setOtp(otp);
            existingPending.setOtpGeneratedTime(now);
            existingPending.setExpiryDate(now.plusMinutes(5));
            pendingPasswordResetRepository.save(existingPending);
        }

        otpEventPublisher.notifyOtpGenerated(email, otp);
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

    public SignedJWT verifyToken(String token) throws Exception {
        return jwtTokenService.verifyToken(token);
    }
}
