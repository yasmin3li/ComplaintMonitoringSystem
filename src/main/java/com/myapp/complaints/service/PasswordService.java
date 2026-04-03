package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.PasswordResetTokenRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.ChangePasswordRequest;
import com.myapp.complaints.dto.ForgotPasswordRequestDTO;
import com.myapp.complaints.dto.ResetPasswordRequestDTO;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.enums.AccountStatus;
import com.myapp.complaints.exceptionHandller.ApiException;
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
                                .orElseThrow(() ->
                                        new ApiException( "Account not found",HttpStatus.NOT_FOUND))
                );


//        boolean validLink = restLinkService.validLink(account, dto.token());
//        if (!validLink) {
//            return new ApiResponseDto<>(
//                    false,
//                    "Invalid reset link",
//                    null
//            );
//        }

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
    public ApiResponseDto<?> changePassword(Authentication auth, ChangePasswordRequest req) {

        Account account = accountRepo.findByEmail(auth.getName())
                .orElseThrow(() ->
                        new ApiException( "Account not found",HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(req.currentPassword(), account.getPassword())) {

            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        account.setPassword(validateAndEncodePassword(req.newPassword()));
        account.setStatus(AccountStatus.ACTIVATED);
        account.setMustChangePassword(false);
        accountRepo.save(account);

        return new ApiResponseDto<>(true, "Password changed successfully", null);
    }

    private  String validateAndEncodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ApiException("Password cannot be empty",HttpStatus.BAD_REQUEST);
        }
        if (rawPassword.length() < 8) {
            throw new ApiException("Password must be at least 8 characters long",HttpStatus.BAD_REQUEST);
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new ApiException("Password must contain at least one uppercase letter",HttpStatus.BAD_REQUEST);
        }
        if (!rawPassword.matches(".*[a-z].*")) {
            throw new ApiException("Password must contain at least one lowercase letter",HttpStatus.BAD_REQUEST);
        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new ApiException("Password must contain at least one digit",HttpStatus.BAD_REQUEST);
        }
        if (!rawPassword.matches(".*[!@#$%^&*].*")) {
            throw new ApiException("Password must contain at least one special character",HttpStatus.BAD_REQUEST);
        }
        return passwordEncoder.encode(rawPassword);
    }


}



