package com.movie.cinema_booking_backend.service.payment.createurl.proxy;

import com.movie.cinema_booking_backend.enums.PaymentMethod;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.service.IPayment;
import com.movie.cinema_booking_backend.service.impl.PaymentService;
import com.movie.cinema_booking_backend.service.payment.createurl.facade.PaymentFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * PaymentProxy — Proxy Pattern cho Payment service.
 *
 * <p>Xử lý validation phương thức thanh toán và điều phối giữa
 * PaymentFacade và PaymentService — không chứa business logic thanh toán.
 *
 * <p>Design Pattern — Proxy: Thêm validation layer trước khi gọi service thực.
 * <p>SOLID — Single Responsibility: Chỉ validate và delegate, không tính toán.
 */
@Slf4j
@Service
public class PaymentProxy implements IPayment {

    private final IPayment paymentService;
    private final PaymentFacade paymentFacade;

    public PaymentProxy(PaymentService paymentService, PaymentFacade paymentFacade) {
        this.paymentService = paymentService;
        this.paymentFacade = paymentFacade;
    }

    /**
     * Tạo URL thanh toán sau khi validate method và đăng ký booking.
     *
     * @param method  phương thức thanh toán (VNPAY, MOMO, v.v.)
     * @param request thông tin thanh toán
     * @return URL thanh toán được tạo
     */
    @Override
    public String createPaymentUrl(String method, PaymentRequest request) {
        if (method == null || method.isBlank()) {
            log.error("Phương thức thanh toán không hợp lệ: {}", method);
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }

        String normalizedMethod = method.trim().toUpperCase();

        // Validate phương thức có trong danh sách hỗ trợ
        boolean isValid = Arrays.stream(PaymentMethod.values())
                .anyMatch(m -> m.name().equalsIgnoreCase(normalizedMethod));

        if (!isValid) {
            log.error("Phương thức thanh toán không được hỗ trợ: {}", normalizedMethod);
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }

        log.info("Tạo URL thanh toán cho phương thức: {}", normalizedMethod);

        int result = paymentFacade.createPayment(normalizedMethod, request.getDescription(), request.getBookingId());

        if (result == 0) {
            log.warn("Booking đã tồn tại trong payment hoặc có lỗi khi thêm booking vào payment");
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        log.info("Booking đã được thêm vào payment thành công, bookingId={}", request.getBookingId());
        return paymentService.createPaymentUrl(normalizedMethod, request);
    }
}
