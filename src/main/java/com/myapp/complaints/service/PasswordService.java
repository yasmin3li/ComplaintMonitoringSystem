package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
import com.myapp.complaints.dto.ApiResponseDto;
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
    public ApiResponseDto<Object> resetPassword(ResetPasswordRequestDTO dto) {

        Account account = accountRepo.findByEmail(dto.emailOrPhone())
                .orElseGet(() ->
                        accountRepo.findByPhoneNumber(dto.emailOrPhone())
                                .orElse(null)
//                                .orElseThrow(() ->
//                                        new ResponseStatusException(
//                                                HttpStatus.NOT_FOUND,
//                                                "Account not found"
//                                        ))
                );
        if (account == null) {
            return new ApiResponseDto<Object>(
                    false,
                    "Account not found",
                    null);
        }

        boolean validLink = restLinkService.validLink(account, dto.token());
        if (!validLink) {
            return new ApiResponseDto<>(
                    false,
                    "Invalid reset link",
                    null
            );
        }

        account.setPassword(validateAndEncodePassword(dto.newPassword()));
        //account.setMustChangePassword(false);
        //account.setStatus(AccountStatus.ACTIVATED);
        accountRepo.save(account);
        return new ApiResponseDto<>(
                true,
                "Password reset successfully",
                null
        );
//        passwordResetTokenRepo.delete(resetToken);
    }

    @Transactional
    public ApiResponseDto<Object> changePassword(Authentication auth, ChangePasswordRequest req) {

        Account account = accountRepo.findByEmail(auth.getName()).orElse(null);
//                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account == null) {
            return new ApiResponseDto<>(false, "Account not found", null);
        }

        if (!passwordEncoder.matches(req.currentPassword(), account.getPassword())) {
//            throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Current password incorrect");
            return new ApiResponseDto<>(false, "Current password is incorrect", null);
        }

        account.setPassword(validateAndEncodePassword(req.newPassword()));
        account.setStatus(AccountStatus.ACTIVATED);
        account.setMustChangePassword(false);
        accountRepo.save(account);

        return new ApiResponseDto<>(true, "Password changed successfully", null);
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



