package com.movie.cinema_booking_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.cinema_booking_backend.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String transactionId; // Mã giao dịch của hệ thống mình sinh ra (ví dụ: GD12345)

    // --- CÁC TRƯỜNG CỦA ĐỐI TÁC TRẢ VỀ ---
    private String transactionNo; // Mã giao dịch của VNPAY/MOMO
    private String transactionDate;
    private String responseCode;
    private String requestId;

    // json phản hồi gốc
    @Column(columnDefinition = "TEXT")
    private String rawResponse;
}
