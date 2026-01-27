package com.myapp.complaints.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verification Code For Email Service");
        message.setText("Your verification code is: " + code);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send verification password email: " + e.getMessage());
            // لا تجعلي التطبيق ينهار إذا فشل الإيميل، أكملي عملية التسجيل
        }
//        mailSender.send(message);
    }

    public void sendResetLink(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
//            mailSender.send(message);
            try {
                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Failed to send reset password email: " + e.getMessage());
                // لا تجعلي التطبيق ينهار إذا فشل الإيميل، أكملي عملية التسجيل
            }
            System.out.println("Reset password email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send reset password email: " + e.getMessage());
        }
    }
}

