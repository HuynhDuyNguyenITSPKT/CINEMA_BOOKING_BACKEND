package com.movie.cinema_booking_backend.service.payment.createurl.facade;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.entity.Payment;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.PaymentMethod;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.BookingRepository;
import com.movie.cinema_booking_backend.repository.PaymentRepository;
import com.movie.cinema_booking_backend.response.PaymentCallbackResponse;
import com.movie.cinema_booking_backend.service.IEmailService;
import com.movie.cinema_booking_backend.service.payment.MoMoService;
import com.movie.cinema_booking_backend.service.payment.VNPayService;


@Service
public class PaymentFacade {
    private static final Logger log = LoggerFactory.getLogger(PaymentFacade.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final VNPayService vnPayService;
    private final MoMoService moMoService;
    private final IEmailService emailService;
    private final Map<String, String> creatorEmailByBookingId = new ConcurrentHashMap<>();

    public PaymentFacade(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            VNPayService vnPayService,
            MoMoService moMoService,
            IEmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.vnPayService = vnPayService;
        this.moMoService = moMoService;
        this.emailService = emailService;
    }

    @Transactional
    public int createPayment(String paymentMethod, String message , String bookingId) {
        return addBookingToPayment(paymentMethod, message, bookingId);
    }

    public void rememberPaymentCreator(String bookingId, String creatorEmail) {
        if (bookingId == null || bookingId.isBlank() || creatorEmail == null || creatorEmail.isBlank()) {
            return;
        }
        creatorEmailByBookingId.put(bookingId, creatorEmail);
    }

    @Transactional
    public int addBookingToPayment(String paymentMethod, String message, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_INVALID_REQUEST));

        if (booking.getStatus() == BookingStatus.SUCCESS) {
            return 0;
        }

        if (paymentRepository.findByBooking_Id(bookingId).isPresent()) {
            return 2;
        }

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.valueOf(paymentMethod.trim().toUpperCase()))
                .booking(booking)
                .message(message)
                .paytime(LocalDateTime.now())
                .build();
        
        paymentRepository.save(payment);
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);
        return 1;
    }

    public PaymentCallbackResponse processPaymentCallback(String method, Map<String, String> params) {
        String normalizedMethod = method == null ? "" : method.trim().toLowerCase();
        boolean verified = verifyByMethod(normalizedMethod, params);
        Map<String, String> result = handlePaymentCallback(normalizedMethod, params, verified);

        String paymentStatus = result.getOrDefault("paymentStatus", "KHONG_THANH_TOAN");
        String message = resolveMessage(paymentStatus);

        return PaymentCallbackResponse.builder()
                .success("THANH_TOAN_THANH_CONG".equals(paymentStatus))
                .message(message)
                .data(result)
                .build();
    }

    @Transactional
    public Map<String, String> handlePaymentCallback(String method, Map<String, String> params, boolean verified) {
        String normalizedMethod = method == null ? "" : method.trim().toLowerCase();
        String bookingId = extractBookingId(normalizedMethod, params);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_INVALID_REQUEST));

        String gatewayCode = extractGatewayCode(normalizedMethod, params);
        String paymentState = resolvePaymentState(verified, normalizedMethod, gatewayCode);

        if ("THANH_TOAN_THANH_CONG".equals(paymentState)) {
            booking.setStatus(BookingStatus.SUCCESS);
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
        }
        bookingRepository.save(booking);

        paymentRepository.findByBooking_Id(bookingId).ifPresent(payment -> {
            payment.setMessage(paymentState);
            payment.setPaytime(LocalDateTime.now());
            paymentRepository.save(payment);
        });

        String creatorEmail = creatorEmailByBookingId.remove(bookingId);
        sendPaymentResultEmail(creatorEmail, booking, paymentState, normalizedMethod, gatewayCode);

        Map<String, String> result = new HashMap<>();
        result.put("bookingId", bookingId);
        result.put("paymentStatus", paymentState);
        result.put("method", normalizedMethod);
        result.put("gatewayCode", gatewayCode);
        return result;
    }

    private String resolvePaymentState(boolean verified, String method, String gatewayCode) {
        if (!verified) {
            return "HUY";
        }

        if ("vnpay".equals(method)) {
            if ("00".equals(gatewayCode)) {
                return "THANH_TOAN_THANH_CONG";
            }
            if ("24".equals(gatewayCode)) {
                return "HUY";
            }
            return "HUY";
        }

        if ("momo".equals(method)) {
            if ("0".equals(gatewayCode)) {
                return "THANH_TOAN_THANH_CONG";
            }
            if ("1006".equals(gatewayCode)) {
                return "HUY";
            }
            return "HUY";
        }

        throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
    }

    private boolean verifyByMethod(String method, Map<String, String> params) {
        switch (method) {
            case "vnpay":
                return vnPayService.verify(params);
            case "momo":
                return moMoService.verify(params);
            default:
                return false;
        }
    }

    private String resolveMessage(String paymentStatus) {
        switch (paymentStatus) {
            case "THANH_TOAN_THANH_CONG":
                return "Thanh toán thành công";
            case "HUY":
                return "Hủy thanh toán";
            default:
                return "Hủy thanh toán";
        }
    }

    private String extractBookingId(String method, Map<String, String> params) {
        if ("vnpay".equals(method)) {
            String bookingId = params.getOrDefault("vnp_TxnRef", "").trim();
            if (!bookingId.isEmpty()) {
                return bookingId;
            }
        }

        if ("momo".equals(method)) {
            String bookingId = params.getOrDefault("orderId", "").trim();
            if (!bookingId.isEmpty()) {
                return bookingId;
            }
        }

        throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
    }

    private String extractGatewayCode(String method, Map<String, String> params) {
        if ("vnpay".equals(method)) {
            String transactionStatus = params.getOrDefault("vnp_TransactionStatus", "").trim();
            if (!transactionStatus.isEmpty()) {
                return transactionStatus;
            }
            return params.getOrDefault("vnp_ResponseCode", "").trim();
        }

        if ("momo".equals(method)) {
            return params.getOrDefault("resultCode", "").trim();
        }

        throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    public void deletePaymentById(Long id) {
        paymentRepository.deleteById(id);
    }

    private void sendPaymentResultEmail(String creatorEmail, Booking booking, String paymentState, String paymentMethod, String gatewayCode) {
        String to = creatorEmail;
        if (to == null || to.isBlank()) {
            to = booking.getUser() != null ? booking.getUser().getEmail() : null;
        }

        if (to == null || to.isBlank()) {
            log.warn("Khong the gui email ket qua thanh toan cho booking {} vi thieu email nguoi dung", booking.getId());
            return;
        }

        String bookingId = booking.getId();
        String amount = booking.getTotalAmount() == null ? "0" : booking.getTotalAmount().toPlainString();

        try {
            if ("THANH_TOAN_THANH_CONG".equals(paymentState)) {
                emailService.sendPaymentSuccessEmail(to, bookingId, paymentMethod.toUpperCase(), amount);
            } else {
                String reason = "Gateway code: " + (gatewayCode == null || gatewayCode.isBlank() ? "N/A" : gatewayCode);
                emailService.sendPaymentFailedEmail(to, bookingId, paymentMethod.toUpperCase(), amount, reason);
            }
        } catch (Exception ex) {
            log.error("Gui email ket qua thanh toan that bai cho booking {}: {}", bookingId, ex.getMessage());
        }
    }
}
