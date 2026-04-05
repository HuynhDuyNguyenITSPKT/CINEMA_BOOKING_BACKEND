package com.movie.cinema_booking_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.UserRepository;
import com.movie.cinema_booking_backend.request.AdminAccountUpdateRequest;
import com.movie.cinema_booking_backend.request.UpdateProfileRequest;
import com.movie.cinema_booking_backend.response.AdminUserAccountResponse;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IUserService;
import com.movie.cinema_booking_backend.service.auth.observer.AccountStatusChangedEvent;
import com.movie.cinema_booking_backend.service.auth.observer.AccountStatusSubject;

import jakarta.transaction.Transactional;

@Service
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountStatusSubject accountStatusSubject;

    public UserService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            AccountStatusSubject accountStatusSubject
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.accountStatusSubject = accountStatusSubject;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .dateOfBirth(user.getDateOfBirth())
                        .role(user.getAccount().getRole())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Authentication authentication, UpdateProfileRequest request) {
        Account account = accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User user = account.getUser();

        if (request.getDateOfBirth().isAfter(java.time.LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_DATE_OF_BIRTH);
        }

        if (userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }

        if (userRepository.existsByPhoneAndIdNot(request.getPhone(), user.getId())) {
            throw new AppException(ErrorCode.PHONE_EXISTS);
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDateOfBirth(request.getDateOfBirth());
        User saved = userRepository.save(user);

        return UserResponse.builder()
                .id(saved.getId().toString())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .phone(saved.getPhone())
                .dateOfBirth(saved.getDateOfBirth())
                .role(account.getRole())
                .build();
    }

    @Override
    public Page<AdminUserAccountResponse> getUsersForAdmin(int page, int size, String keyword) {
        String value = keyword == null ? "" : keyword.trim();
        return accountRepository
                .findByUsernameContainingIgnoreCaseOrUser_FullNameContainingIgnoreCase(
                        value,
                        value,
                        PageRequest.of(page, size)
                )
            .map(this::mapToUserAccountResponse);
    }

    @Override
    @Transactional
    public AdminUserAccountResponse updateUserAccountByAdmin(Long userId, AdminAccountUpdateRequest request) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean statusChanged = false;

        if (request.getRole() != null) {
            account.setRole(request.getRole());
        }

        if (request.getActive() != null) {
            boolean nextStatus = request.getActive();
            statusChanged = account.isActive() != nextStatus;
            account.setActive(nextStatus);
        }

        Account savedAccount = accountRepository.save(account);

        if (statusChanged) {
            User savedUser = savedAccount.getUser();
            accountStatusSubject.notifyObservers(new AccountStatusChangedEvent(
                    savedAccount.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    savedAccount.isActive()
            ));
        }

        return mapToUserAccountResponse(savedAccount);
    }

    private AdminUserAccountResponse mapToUserAccountResponse(Account account) {
        User user = account.getUser();
        return AdminUserAccountResponse.builder()
                .userId(user.getId().toString())
                .username(account.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .role(account.getRole())
                .active(account.isActive())
                .build();
    }
}
