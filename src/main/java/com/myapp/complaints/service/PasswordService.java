package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
import com.myapp.complaints.dto.ChangePasswordRequest;
import com.myapp.complaints.dto.ForgotPasswordRequestDTO;
import com.myapp.complaints.dto.ResetPasswordRequestDTO;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.enums.AccountStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final AccountRepo accountRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final RestLinkService restLinkService;


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

        restLinkService.validLink(account,dto.token());

        account.setPassword(validateAndEncodePassword(dto.newPassword()));
        //account.setMustChangePassword(false);
        //account.setStatus(AccountStatus.ACTIVATED);

        accountRepo.save(account);

//        passwordResetTokenRepo.delete(resetToken);
    }

    @Transactional
    public void changePassword(Authentication auth, ChangePasswordRequest req) {

        Account account = accountRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!passwordEncoder.matches(req.currentPassword(), account.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Current password incorrect");
        }

        account.setPassword(validateAndEncodePassword(req.newPassword()));
        account.setStatus(AccountStatus.ACTIVATED);
        account.setMustChangePassword(false);
        accountRepo.save(account);
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
        if (!rawPassword.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!rawPassword.matches(".*[!@#$%^&*].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
        return passwordEncoder.encode(rawPassword);
    }


}



