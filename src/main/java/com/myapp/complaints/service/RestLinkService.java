package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
import com.myapp.complaints.dto.ForgotPasswordRequestDTO;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.PasswordResetToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class RestLinkService {
    private final EmailService emailService;
    private final AccountRepo accountRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;

    public void sendResetLink(ForgotPasswordRequestDTO dto) {

        Account account = accountRepo.findByEmail(dto.emailOrPhone())
                .orElseGet(() ->
                        accountRepo.findByPhoneNumber(dto.emailOrPhone())
                                .orElseThrow(() ->
                                        new RuntimeException("Account not found")
                                )
                );

        passwordResetTokenRepo.deleteByAccount(account);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setAccount(account);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepo.save(resetToken);
        System.out.println(resetToken);

//        emailService.sendVerificationCode(account.getEmail(),token);
//TODO after link with front
        try {
            emailService.sendResetLink(
                    account.getEmail(),
                    "Reset your password",
                    "Click the link:\n" +
                            "http://localhost:5173/reset-password?token=" + token
            );
        } catch (RuntimeException e) {
            throw new RuntimeException("failed to send rest link ");
        }
    }
}
