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

    public void sendEmail(String to, String subject, String htmlMessage
    ) {
        MimeMessage message =
                mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(generateEmailTemplate(to, subject, htmlMessage), true
            );
            mailSender.send(message);

        } catch (MessagingException e) {

            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String generateEmailTemplate(
            String email,
            String subject,
            String htmlMessage
    ) {

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta
                        name="viewport"
                        content="width=device-width, initial-scale=1.0">
                    <title>Vast World</title>
                </head>
              
                <body style="
                    font-family:Arial,sans-serif;
                    line-height:1.6;
                    margin:0;
                    padding:0;
                    background:#f4f4f4;
                    color:#333;
                ">
                <table
                    role="presentation"
                    style="
                        width:100%%;
                        border-collapse:collapse;
                        padding:20px;
                    ">
                <tr>
                <td>
                <table
                    role="presentation"
                    style="
                        max-width:600px;
                        margin:auto;
                        background:white;
                        border-radius:10px;
                        overflow:hidden;
                        box-shadow:
                            0 2px 10px rgba(0,0,0,0.1);
                    ">
                <!-- Header -->
                <tr>
                <td style="
                    background:#0078D4;
                    padding:25px;
                    text-align:center;
                ">
                <h1 style="
                    margin:0;
                    color:white;
                ">
                    Vast World
                </h1>
                </td>
                </tr>
                <!-- Content -->
                <tr>
                <td style="padding:30px;">
                <h2 style="
                    color:#0078D4;
                    margin-top:0;
                ">
                    %s
                </h2>
                <p>
                    Hello,
                    <strong>%s</strong>
                </p>
                %s
                <p style="
                    margin-top:30px;
                ">
                    Best regards,<br>
                    <strong>
                        The Vast World Team
                    </strong>
                </p>
                </td>
                </tr>
                <!-- Footer -->
                <tr>
                <td style="
                    background:#f8f8f8;
                    padding:20px;
                    text-align:center;
                    font-size:13px;
                    color:#888;
                ">
                <p style="margin:0;">
                    This is an automated email.
                </p>
                <p style="
                    margin-top:10px;
                ">
                    © 2026 Vast World.
                    All rights reserved.
                </p>
                </td>
                </tr>
                </table>
                </td>
                </tr>
                </table>
                </body>
                </html>
                """.formatted(
                subject,
                email,
                htmlMessage
        );
    }
}