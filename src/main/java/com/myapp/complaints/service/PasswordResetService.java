package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
import com.myapp.complaints.dto.ForgotPasswordRequestDTO;
import com.myapp.complaints.dto.ResetPasswordRequestDTO;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.PasswordResetToken;
import com.myapp.complaints.enums.AccountStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final AccountRepo accountRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final PasswordEncoder passwordEncoder;
//    private final EmailService emailService;

    /**
     * Send reset password link
     */
    public void sendResetLink(ForgotPasswordRequestDTO dto) {

        Account account = accountRepo.findByEmail(dto.emailOrPhone())
                .orElseGet(() ->
                        accountRepo.findByPhoneNumber(dto.emailOrPhone())
                                .orElseThrow(() ->
                                        new RuntimeException("Account not found")
                                )
                );

        // حذف أي توكن قديم
        passwordResetTokenRepo.deleteByAccount(account);

        // إنشاء توكن جديد
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setAccount(account);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepo.save(resetToken);

        // إرسال الإيميل (لاحقًا)
//        emailService.send(
//                account.getEmail(),
//                "Reset your password",
//                "Click the link:\n" +
//                        "https://frontend/reset-password?token=" + token
//        );
    }

    /**
     * Reset password using token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO dto) {

        Account account = accountRepo.findByEmail(dto.emailOrPhone())
                .orElseGet(() ->
                        accountRepo.findByPhoneNumber(dto.emailOrPhone())
                                .orElseThrow(() ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Account not found"
                                        ))
                );
        PasswordResetToken resetToken =
                passwordResetTokenRepo.findByTokenAndAccount(dto.token(),account)
                        .orElseThrow(() ->
                                new RuntimeException("Invalid reset token")
                        );

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        account.setPassword(validateAndEncodePassword(dto.newPassword()));
        account.setMustChangePassword(false);
        account.setStatus(AccountStatus.ACTIVATED);

        accountRepo.save(account);

        // حذف التوكن بعد الاستخدام
        passwordResetTokenRepo.delete(resetToken);
    }

    private  String validateAndEncodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
//        if (!rawPassword.matches(".*[a-z].*")) {
//            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
//        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!rawPassword.matches(".*[!@#$%^&*].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
//        if (!rawPassword.matches(".*[]\\[!@#$%^&*()_+\\-={};':\"\\\\|,.<>/?].*")) {
//            throw new IllegalArgumentException("Password must contain at least one special character");
//        }
        // تشفير كلمة المرور
        return passwordEncoder.encode(rawPassword);
    }


}



