package com.movie.cinema_booking_backend.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;
import com.movie.cinema_booking_backend.service.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

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
    public void sendGeneratedPasswordEmail(String to, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("[Cinema Booking] Mat khau tam thoi");
        message.setText(
            "Chao ban,\n\n" +
            "He thong da tao mat khau tam thoi cho tai khoan cua ban:\n" +
            "Mat khau: " + temporaryPassword + "\n\n" +
            "Vui long dang nhap va doi mat khau ngay de dam bao an toan tai khoan.\n\n" +
            "Tran trong,\nDoi ngu Cinema Booking"
        );
        mailSender.send(message);
    }

    @Override
    public void sendAccountStatusChangedEmail(String to, String fullName, boolean isActive) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        if (isActive) {
            message.setSubject("[Cinema Booking] Tai khoan da duoc mo khoa");
            message.setText(
                "Chao " + fullName + ",\n\n" +
                "Tai khoan cua ban da duoc mo khoa va co the su dung lai binh thuong.\n\n" +
                "Tran trong,\nDoi ngu Cinema Booking"
            );
        } else {
            message.setSubject("[Cinema Booking] Tai khoan da bi khoa");
            message.setText(
                "Chao " + fullName + ",\n\n" +
                "Tai khoan cua ban da bi khoa tam thoi boi quan tri vien.\n" +
                "Vui long lien he ho tro neu ban can them thong tin.\n\n" +
                "Tran trong,\nDoi ngu Cinema Booking"
            );
        }
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

    @Override
    public void sendTicketQrEmail(String to, String bookingId, String movieName, String amount, List<String> ticketIds) {
        if (ticketIds == null || ticketIds.isEmpty()) {
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("[Cinema Booking] Ve QR - Don " + bookingId);

            StringBuilder ticketsHtml = new StringBuilder();
            for (String ticketId : ticketIds) {
                String qrContent = "CB_TICKET:" + ticketId;
                String qrDataUri = generateQrDataUri(qrContent);

                ticketsHtml.append("""
                    <div style="border:1px solid #eee;border-radius:8px;padding:12px;margin-bottom:12px;">
                        <p style="margin:0 0 8px 0;"><b>Ma ve:</b> %s</p>
                        <img src="%s" alt="QR %s" width="180" height="180" style="display:block;"/>
                        <p style="margin:8px 0 0 0;font-size:12px;color:#666;">Noi dung QR: %s</p>
                    </div>
                    """.formatted(ticketId, qrDataUri, ticketId, qrContent));
            }

            String htmlTemplate = """
                <div style="font-family: Arial, sans-serif; background-color: #f4f6f8; padding: 20px;">
                    <div style="max-width: 680px; margin: auto; background: white; border-radius: 10px; padding: 20px;">
                        <h2 style="color: #0d6efd;">Ve dien tu cua ban</h2>
                        <p>Thanh toan don <b>%s</b> da thanh cong.</p>
                        <p><b>Phim:</b> %s</p>
                        <p><b>Tong tien:</b> %s</p>
                        <p style="margin-top: 16px;"><b>Danh sach QR ve:</b></p>
                        %s
                        <hr>
                        <p style="font-size: 12px; color: gray;">Vui long giu kin ma QR va dua ma nay cho nhan vien rap khi check-in.</p>
                    </div>
                </div>
                """;

            String html = htmlTemplate.formatted(
                    bookingId,
                    movieName == null || movieName.isBlank() ? "N/A" : movieName,
                    amount,
                    ticketsHtml
            );

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateQrDataUri(String content) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 220, 220);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
