package com.movie.cinema_booking_backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.enums.Role;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByUsername(String username);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByUserEmail(String email);

    Optional<Account> findByUserId(Long userId);

    long countByRole(Role role);

    @Query("SELECT a FROM Account a JOIN a.user u " +
            "WHERE (:keyword = '' OR " +
            "LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:email = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:phone = '' OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :phone, '%'))) " +
            "AND (:status IS NULL OR a.isActive = :status)")
    Page<Account> searchAccountsForAdmin(
            @Param("keyword") String keyword,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("status") Boolean status,
            Pageable pageable
    );
}
