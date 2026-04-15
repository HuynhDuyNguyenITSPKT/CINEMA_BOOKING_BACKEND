package com.movie.cinema_booking_backend.service.payment.createurl.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.enums.PaymentMethod;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.service.IPayment;
import com.movie.cinema_booking_backend.service.impl.PaymentService;
import com.movie.cinema_booking_backend.service.payment.createurl.facade.PaymentFacade;

@Service
public class PaymentProxy implements IPayment{
    
    private static final Logger log = LoggerFactory.getLogger(PaymentProxy.class);

    private final IPayment paymentService;
    private final PaymentFacade paymentFacade;    

    public PaymentProxy(PaymentService paymentService , PaymentFacade paymentFacade) {
        this.paymentService = paymentService;
        this.paymentFacade = paymentFacade;
    }

    @Override
    public String createPaymentUrl(String method, PaymentRequest request){
        if (method == null || method.isEmpty()) {
            log.error("Phương thức thanh toán không hợp lệ: {}", method);
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
        }

        boolean isValid = false;
        for (PaymentMethod m : PaymentMethod.values()) {
            if (m.name().equalsIgnoreCase(method)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            log.error("Phương thức thanh toán không được hỗ trợ: {}", method);
            throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ");
        }

        method = method.toUpperCase();

        method = method.trim();

        log.info("Tạo URL thanh toán cho phương thức: {}", method);
        
        int number = paymentFacade.createPayment(method, request.getDescription(), request.getBookingId());

        if (number == 0) {
            log.warn("Booking đã thanh toán thành công trước đó");
            throw new AppException(ErrorCode.PAYMENTSUCCESS);
        } else if (number == 2) {
            log.warn("Booking đã có thanh toán nhưng chưa thành công");
            throw new AppException(ErrorCode.PAYMENT_ALREADY_EXISTS_UNSUCCESS);
        }
        else {
            log.info("Booking đã được thêm vào payment thành công");
        }

        return paymentService.createPaymentUrl(method, request);
    }
    
}