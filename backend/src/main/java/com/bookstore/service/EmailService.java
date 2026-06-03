package com.bookstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bookstore.model.Product;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

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

    /**
     * Notify a customer that one or more products on their wishlist are now discounted
     * (Req 11). Sends a single HTML email listing each affected product's old price,
     * new price, and percentage off. Failures are logged and never break the caller.
     */
    @Async
    public void sendDiscountNotification(String toEmail, String customerName, List<Product> products) {
        if (products == null || products.isEmpty()) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Discounts on your wishlist — Online Bookstore");

            StringBuilder rows = new StringBuilder();
            for (Product p : products) {
                String oldPrice = p.getOriginalPrice() != null ? p.getOriginalPrice().toPlainString() : "-";
                String rate = p.getDiscountRate() != null
                        ? p.getDiscountRate().stripTrailingZeros().toPlainString() : "0";
                rows.append("<li><strong>").append(p.getName()).append("</strong>: ")
                    .append("was ₺").append(oldPrice)
                    .append(", now ₺").append(p.getPrice().toPlainString())
                    .append(" (").append(rate).append("% off)")
                    .append("</li>");
            }

            helper.setText(
                "<div style='font-family:sans-serif;'>" +
                "<h2>Good news, " + customerName + "!</h2>" +
                "<p>Items on your wishlist are now on discount:</p>" +
                "<ul>" + rows + "</ul>" +
                "<br/><p>— Online Bookstore Team</p>" +
                "</div>",
                true
            );

            mailSender.send(message);
            log.info("Discount notification sent to {} for {} product(s)", toEmail, products.size());

        } catch (MessagingException e) {
            log.error("Failed to send discount notification to {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending discount notification: {}", e.getMessage());
        }
    }
}
