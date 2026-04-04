package com.movie.cinema_booking_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.service.IEmailService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Ma OTP xac thuc - Cinema Booking");
        message.setText("Chao ban,\n\nMa OTP cua ban la: " + otp
                + "\nMa nay co hieu luc trong 5 phut. Vui long khong chia se ma nay cho bat ky ai.");
        mailSender.send(message);
    }

    @Override
    public void sendNewMovieNotificationEmail(String to, String movieTitle, String description, String releaseDate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("[Cinema Booking] Phim moi ra mat: " + movieTitle);
        message.setText(
            "Chao ban,\n\n" +
            "Chung toi vui mung thong bao phim moi vua duoc them vao he thong:\n\n" +
            "Ten phim : " + movieTitle + "\n" +
            "Ngay cong chieu: " + releaseDate + "\n" +
            "Mo ta     : " + description + "\n\n" +
            "Truy cap Cinema Booking de dat ve ngay!\n\n" +
            "Tran trong,\nDoi ngu Cinema Booking"
        );
        mailSender.send(message);
    }

    @Override
    public void sendMovieUpdatedNotificationEmail(String to, String movieTitle, String description) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("[Cinema Booking] Thong tin phim duoc cap nhat: " + movieTitle);
        message.setText(
            "Chao ban,\n\n" +
            "Thong tin phim \"" + movieTitle + "\" vua duoc cap nhat:\n\n" +
            "Mo ta moi: " + description + "\n\n" +
            "Truy cap Cinema Booking de xem chi tiet.\n\n" +
            "Tran trong,\nDoi ngu Cinema Booking"
        );
        mailSender.send(message);
    }

    @Override
    public void sendMovieDeletedNotificationEmail(String to, String movieTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("[Cinema Booking] Phim da bi go: " + movieTitle);
        message.setText(
            "Chao ban,\n\n" +
            "Phim \"" + movieTitle + "\" da duoc go khoi he thong Cinema Booking.\n" +
            "Neu ban da dat ve, vui long lien he de duoc ho tro.\n\n" +
            "Tran trong,\nDoi ngu Cinema Booking"
        );
        mailSender.send(message);
    }

    @Override
    public void sendPaymentSuccessEmail(String to, String bookingId, String paymentMethod, String amount) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("[Cinema Booking] Thanh toán thành công - Đơn " + bookingId);

            String htmlTemplate = """
                <div style="font-family: Arial, sans-serif; background-color: #f4f6f8; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 20px;">
                        
                        <h2 style="color: #28a745;">🎉 Thanh toán thành công</h2>
                        
                        <p>Chào bạn,</p>
                        <p>Đơn đặt vé của bạn đã được thanh toán thành công.</p>

                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td><b>Mã đơn:</b></td>
                                <td>{{BOOKING_ID}}</td>
                            </tr>
                            <tr>
                                <td><b>Phương thức:</b></td>
                                <td>{{PAYMENT_METHOD}}</td>
                            </tr>
                            <tr>
                                <td><b>Số tiền:</b></td>
                                <td style="color: #28a745;"><b>{{AMOUNT}}</b></td>
                            </tr>
                        </table>

                        <p style="margin-top: 20px;">Cảm ơn bạn đã sử dụng dịch vụ 🎬</p>

                        <hr>
                        <p style="font-size: 12px; color: gray;">Cinema Booking Team</p>
                    </div>
                </div>
            """;

            String html = htmlTemplate
                    .replace("{{BOOKING_ID}}", bookingId)
                    .replace("{{PAYMENT_METHOD}}", paymentMethod)
                    .replace("{{AMOUNT}}", amount);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPaymentFailedEmail(String to, String bookingId, String paymentMethod, String amount, String reason) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("[Cinema Booking] Thanh toán thất bại - Đơn " + bookingId);

            String htmlTemplate = """
                <div style="font-family: Arial, sans-serif; background-color: #f4f6f8; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; padding: 20px;">
                        
                        <h2 style="color: #dc3545;">❌ Thanh toán thất bại</h2>
                        
                        <p>Chào bạn,</p>
                        <p>Rất tiếc, đơn đặt vé của bạn chưa thanh toán thành công.</p>

                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td><b>Mã đơn:</b></td>
                                <td>{{BOOKING_ID}}</td>
                            </tr>
                            <tr>
                                <td><b>Phương thức:</b></td>
                                <td>{{PAYMENT_METHOD}}</td>
                            </tr>
                            <tr>
                                <td><b>Số tiền:</b></td>
                                <td><b>{{AMOUNT}}</b></td>
                            </tr>
                            <tr>
                                <td><b>Lý do:</b></td>
                                <td style="color: #dc3545;">{{REASON}}</td>
                            </tr>
                        </table>

                        <p style="margin-top: 20px;">
                            Bạn có thể thử lại hoặc chọn phương thức thanh toán khác.
                        </p>

                        <hr>
                        <p style="font-size: 12px; color: gray;">Cinema Booking Team</p>
                    </div>
                </div>
            """;

            String html = htmlTemplate
                    .replace("{{BOOKING_ID}}", bookingId)
                    .replace("{{PAYMENT_METHOD}}", paymentMethod)
                    .replace("{{AMOUNT}}", amount)
                    .replace("{{REASON}}", reason);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
