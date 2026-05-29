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
                       <html lang=""en"">
                       <head>
                       <meta charset=""UTF-8"">
                       <meta name=""viewport"" content=""width=device-width, initial-scale=1.0"">
                       <title>Verify Your Vast World Account</title>
                       </head>
                       <body style=""font-family: Arial, sans-serif; line-height: 1.6; color: #333333; margin: 0; padding: 0; background-color: #f4f4f4;"">
                       <table role=""presentation"" style=""width: 100%; border-collapse: collapse;"">
                           <tr>
                               <td style=""padding: 0;"">
                                   <table role=""presentation"" style=""max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);"">
                                       <!-- Header -->
                                       <tr>
                                           <td style=""background-color: #0078D4; padding: 20px; text-align: center;"">
                                               <h1 style=""color: #ffffff; margin: 0; font-size: 28px;"">Vast World</h1>
                                           </td>
                                       </tr>
                                       <!-- Content -->
                                       <tr>
                                           <td style=""padding: 30px;"">
                                               <h2 style=""color: #0078D4; margin-top: 0; margin-bottom: 20px; font-size: 24px;"">{1}</h2>
                                               <p style=""margin-top: 0; margin-bottom: 20px;"">Hello, {0}</p>
                                               <p style=""margin-top: 0; margin-bottom: 20px;"">{2}</p>
                                               <p style=""margin-top: 0; margin-bottom: 20px;"">If you have any questions or need assistance, please don't hesitate to contact our support team at nhanhem2ui@gmail.com.</p>
                                               <p style=""margin-top: 0; margin-bottom: 0;"">Best regards,<br>The VastWorld Team</p>
                                           </td>
                                       </tr>
                                       <!-- Footer -->
                                       <tr>
                                           <td style=""background-color: #f8f8f8; padding: 20px; text-align: center; font-size: 14px; color: #888888;"">
                                               <p style=""margin: 0;"">This is an automated message, please do not reply to this email.</p>
                                               <p style=""margin: 10px 0 0;"">© 2026 VastWolrd. All rights reserved.</p>
                                           </td>
                                       </tr>
                                   </table>
                               </td>
                           </tr>
                       </table>
                       </body>
                       </html>
                """.formatted(subject, email, htmlMessage);
    }
}