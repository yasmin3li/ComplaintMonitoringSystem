package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AccountRepo accountRepo;

    public boolean sendVerificationCode(String to, String code) {

        Account account = accountRepo.findByEmail(to)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Email " + to + " not found")
                );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Balligh _ بَلِّغْ ");
        message.setText("Hello " + account.getUserName() + ", Your verification code for complaints monitoring system is:  " + code);

        try {
            mailSender.send(message);
            return true;
        } catch (Exception e) {
             System.err.println("Failed to send verification password email: " + e.getMessage());
             return false;
//            throw new RuntimeException("Failed to send verification password email: " + e.getMessage());
        }
    }

    public boolean sendResetLink(String toEmail, String subject, String body) {
//        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
//            mailSender.send(message);
            try {
                mailSender.send(message);
                return true;
            } catch (Exception e) {
                System.err.println("Failed to send reset password email: " + e.getMessage());
                return false;
            }
//            System.out.println("Reset password email sent to: " + toEmail);
//        } catch (Exception e) {
//            System.err.println("Failed to send reset password email: " + e.getMessage());
//        }
    }
}

