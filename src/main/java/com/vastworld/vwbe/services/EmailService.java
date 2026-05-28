package com.vastworld.vwbe.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String htmlMessage) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(generateEmailTemplate(to, subject, htmlMessage), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String generateEmailTemplate(String email, String subject, String htmlMessage) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h1>Vast World</h1>
                    <h2>%s</h2>
                    <p>Hello, %s</p>
                    <p>%s</p>
                </body>
                </html>
                """.formatted(subject, email, htmlMessage);
    }
}