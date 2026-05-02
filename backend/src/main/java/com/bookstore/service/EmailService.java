package com.bookstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.math.BigDecimal;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendInvoiceEmail(String toEmail, String customerName,
                                  Long orderId, BigDecimal totalPrice, String pdfPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your Order Invoice #" + orderId + " — Online Bookstore");
            helper.setText(
                "<div style='font-family:sans-serif;'>" +
                "<h2>Thank you for your order, " + customerName + "!</h2>" +
                "<p>Your order <strong>#" + orderId + "</strong> has been placed successfully.</p>" +
                "<p>Total paid: <strong>₺" + totalPrice.toPlainString() + "</strong></p>" +
                "<p>Please find your invoice PDF attached to this email.</p>" +
                "<br/><p>— Online Bookstore Team</p>" +
                "</div>",
                true
            );

            File pdf = new File(pdfPath);
            if (pdf.exists()) {
                helper.addAttachment("invoice-" + orderId + ".pdf", pdf);
            }

            mailSender.send(message);
            log.info("Invoice email sent to {} for order #{}", toEmail, orderId);

        } catch (MessagingException e) {
            log.error("Failed to send invoice email to {} for order #{}: {}", toEmail, orderId, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending invoice email: {}", e.getMessage());
        }
    }
}
